package ed.inf.adbs.minibase.utils;


import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.operators.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Class for producing a query plan. getQueryPlan() is used to initialise the query planner.
 * The function calls the private method buildTree, which builds the tree of operations.
 * The only public method (other than getQueryPlan() ) is getRoot(), which returns the root of
 * the query plan. dump() or getNextTuple() repeatedly, can then be called on the root to
 * evaluate the query.
 */
public class QueryPlan {
    /** Class instance */
    private static QueryPlan plan = null;
    /** Root of the tree of operators */
    private Operator root;
    /** Query to evaluate */
    private Query query;

    /**
     * Method for initialising the query plan and getting the class instance.
     *
     * @param databaseDir path to database directory
     * @param query query to evaluate
     * @return class instance
     */
    public static QueryPlan getQueryPlan(String databaseDir, Query query) {
        if (plan == null) {
            // Catalog is not used in query plan, but initialised for later use
            DatabaseCatalog.getCatalog(databaseDir);

            plan = new QueryPlan();
            plan.query = query;
            buildTree();
        }
        return plan;
    }

    /**
     * Method for the main logic behind building the tree of operations that the
     * query plan consists of.
     */
    private static void buildTree() {
        List<ComparisonAtom> comparisonAtomList = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();

        // Iterate through all atoms
        for (Atom atom:plan.query.getBody()){
            if (atom instanceof RelationalAtom){
                boolean hasConstant = false;
                for (Term t:((RelationalAtom) atom).getTerms()){
                    if (t instanceof Constant) {
                        hasConstant = true;
                        break;
                    }
                }

                // ScanOperator may return FileNotFoundException
                try {
                    // create ScanOperations as the leaf operations
                    // if the atom has a constant, create a new SelectOperation as a parent operation
                    if (hasConstant){
                        ScanOperator child = new ScanOperator(((RelationalAtom) atom).getName(),(RelationalAtom) atom);
                        operators.add(new SelectOperator(child, new ArrayList<>(), child.getAtom()));
                    } else {
                        operators.add(new ScanOperator(((RelationalAtom) atom).getName(),(RelationalAtom) atom));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (atom instanceof ComparisonAtom){
                comparisonAtomList.add((ComparisonAtom) atom);
            } else {
                throw new IllegalArgumentException("Unrecognized atom type");
            }
        }

        // Split ComparisonAtoms into selection conditions and join conditions
        List<ComparisonAtom> selectionComparators = new ArrayList<>();
        List<ComparisonAtom> joinComparators = new ArrayList<>();
        for (ComparisonAtom comp:comparisonAtomList) {
            if (isJoinComparator(comp,operators)){
                joinComparators.add(comp);
            } else {
                selectionComparators.add(comp);
            }
        }

        // if there are selection conditions, create SelectOperations as parents operations
        if (selectionComparators.size()!=0){
            List<Operator> temp = new ArrayList<>();
            for (Operator operator : operators) {
                temp.add(new SelectOperator(operator, selectionComparators, operator.getAtom()));
            }
            operators = temp;
        }

        plan.root = operators.get(0);

        // if there is more than one relation create joins
        if (operators.size()>1){
            operators.remove(0);
            plan.root = createJoins(operators,plan.root,joinComparators);
        }

        SumAggregate agg = plan.query.getHead().getSumAggregate();

        // only push projection when there is no sum aggregate
        if (agg == null){
            plan.root = pushProjection(plan.root, plan.query.getHead().getVariables());
        }

        // if the query has an aggregation the root is a SumOperator, otherwise a ProjectOperator
        if (agg != null){
            plan.root = new SumOperator(plan.root, agg, plan.root.getAtom(), plan.query.getHead().getVariables());
        } else {
            plan.root = new ProjectOperator(plan.root, plan.query.getHead().getVariables(), (plan.root).getAtom());
        }
    }

    /**
     * Method for recursively pushing down projection through the join tree.
     *
     * @param root root of the tree
     * @param variables variables that must be kept
     * @return returns the root operator
     */
    private static Operator pushProjection(Operator root, List<Variable> variables){
        if (root instanceof JoinOperator){
            List<Variable> neededVariables = new ArrayList<>(variables);

            JoinOperator joinRoot = (JoinOperator) root;

            // Add variables from join condition
            if (joinRoot.getCompareAtom()!=null){
                if (!neededVariables.contains((Variable) joinRoot.getCompareAtom().getTerm1()))
                    neededVariables.add((Variable) joinRoot.getCompareAtom().getTerm1());
                if (!neededVariables.contains((Variable) joinRoot.getCompareAtom().getTerm2()))
                    neededVariables.add((Variable) joinRoot.getCompareAtom().getTerm2());
            } else if (joinRoot.getCompareVar()!=null) {
                if (!neededVariables.contains(joinRoot.getCompareVar())) neededVariables.add(joinRoot.getCompareVar());
            }

            // recursively
            Operator pushProjectionLeft = new ProjectOperator(
                    pushProjection(joinRoot.getChildLeft(),neededVariables),
                    neededVariables,joinRoot.getChildLeft().getAtom());
            Operator pushProjectionRight = new ProjectOperator(
                    pushProjection(joinRoot.getChildRight(),neededVariables),
                    neededVariables,joinRoot.getChildRight().getAtom());

            joinRoot.setChildLeft(pushProjectionLeft);
            joinRoot.setChildRight(pushProjectionRight);
            joinRoot.reloadIndex();

            return joinRoot;
        } else {
            return root;
        }
    }

    /**
     * Checks whether the given comparison atom is a join condition. Returns false
     * if one of the terms is a constant OR both of the terms exist in one relation.
     *
     * @param comp comparison atom to test
     * @param operators all operators for which it could be a join condition
     * @return returns true if the comparison atom is a join condition
     */
    private static boolean isJoinComparator(ComparisonAtom comp,List<Operator> operators){
        if (comp.getTerm1() instanceof Constant) return false;
        if (comp.getTerm2() instanceof Constant) return false;

        for (Operator op:operators){
            boolean term1 = op.getAtom().getTerms().contains(comp.getTerm1());
            boolean term2 = op.getAtom().getTerms().contains(comp.getTerm2());
            if (term1 && term2){
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for the root operator of the query plan
     *
     * @return root operator of query plan
     */
    public static Operator getRoot(){
        return plan.root;
    }

    /**
     * Method for creating the join tree. Joins relational atoms from left to right. The leftmost
     * child operation should be passed into the first call, then the method creates the rest of
     * the joins recursively.
     *
     * @param atoms all unjoined atoms in query
     * @param leftChild left child operation for the join
     * @param comp list of join conditions
     * @return returns a join operator
     */
    private static JoinOperator createJoins(List<Operator> atoms, Operator leftChild, List<ComparisonAtom> comp){
        Operator rightChild = atoms.get(0);
        JoinOperator newJoin = new JoinOperator(leftChild, rightChild, comp);

        // call method recursively if newJoin is not the last join
        if (atoms.size()==1){
            return newJoin;
        } else {
            atoms.remove(0);
            return createJoins(atoms, newJoin, comp);
        }
    }
}
