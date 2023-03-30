package ed.inf.adbs.minibase.utils;


import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.operators.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Class for producing a query plan.
 */
public class QueryPlan {
    /** Class instance */
    private static QueryPlan plan = null;
    /** Root of the tree of operators */
    private Operator root;
    /** Query to evaluate */
    private Query query;

    /**
     *
     * @param databaseDir
     * @param query
     * @return
     */
    public static QueryPlan getQueryPlan(String databaseDir, Query query) {
        DatabaseCatalog catalog = DatabaseCatalog.getCatalog(databaseDir); // never used, but initialises the catalog with the directory for use later
        if (plan != null) {
            return plan;
        } else {
            plan = new QueryPlan();
            plan.query = query;
            try {
                buildTree(query);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            return plan;
        }
    }

    /**
     *
     * @param query
     * @throws FileNotFoundException
     */
    private static void buildTree(Query query) throws FileNotFoundException {
        List<ComparisonAtom> comparisonAtomList = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();

        for (Atom atom:query.getBody()){
            if (atom instanceof RelationalAtom){
                boolean hasConstant = false;
                for (Term t:((RelationalAtom) atom).getTerms()){
                    if (t instanceof Constant) {
                        hasConstant = true;
                        break;
                    }
                }

                if (hasConstant){
                    ScanOperator child = new ScanOperator(((RelationalAtom) atom).getName(),(RelationalAtom) atom);
                    operators.add(new SelectOperator(child, new ArrayList<>(), child.getAtom()));
                } else {
                    operators.add(new ScanOperator(((RelationalAtom) atom).getName(),(RelationalAtom) atom));
                }

            } else if (atom instanceof ComparisonAtom){
                comparisonAtomList.add((ComparisonAtom) atom);
            } else {
                throw new IllegalArgumentException("Unrecognized atom type");
            }
        }

        List<ComparisonAtom> selectionComparators = new ArrayList<>();
        List<ComparisonAtom> joinComparators = new ArrayList<>();

        for (ComparisonAtom comp:comparisonAtomList) {
            if (isJoinComparator(comp,operators)){
                joinComparators.add(comp);
            } else {
                selectionComparators.add(comp);
            }
        }

        if (selectionComparators.size()!=0){
            List<Operator> temp = new ArrayList<>();
            for (Operator operator : operators) {
                temp.add(new SelectOperator(operator, selectionComparators, operator.getAtom()));
            }
            operators = temp;
        }

        /*
        List<Variable> neededVariables = new ArrayList<>(query.getHead().getVariables());
        for (ComparisonAtom comp:comparisonAtomList){
            if (comp.getTerm1() instanceof Variable
                    && !neededVariables.contains((Variable) comp.getTerm1()))
                    neededVariables.add((Variable) comp.getTerm1());
            if (comp.getTerm2() instanceof Variable
                    && !neededVariables.contains((Variable) comp.getTerm2()))
                    neededVariables.add((Variable) comp.getTerm2());
        }

         */

        plan.root = operators.get(0);
        if (operators.size()>1){
            operators.remove(0);
            plan.root = createJoins(operators,plan.root,joinComparators,query.getHead().getVariables());
        }

        SumAggregate agg = query.getHead().getSumAggregate();
        if (agg != null){
            plan.root = new SumOperator(plan.root, agg,plan.root.getAtom(),query.getHead().getVariables());
        } else {
            plan.root = new ProjectOperator(plan.root, query.getHead().getVariables(), (plan.root).getAtom());
        }
    }

    /**
     *
     * @param comp
     * @param operators
     * @return
     */
    private static boolean isJoinComparator(ComparisonAtom comp,List<Operator> operators){
        int variables = 0;
        if (comp.getTerm1() instanceof Variable) variables++;
        if (comp.getTerm2() instanceof Variable) variables++;

        if (variables<2){
            return false;
        } else {
            for (Operator op:operators){
                boolean term1 = op.getAtom().getTerms().contains(comp.getTerm1());
                boolean term2 = op.getAtom().getTerms().contains(comp.getTerm2());
                if (term1 && term2){
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Method for creating the join tree. Joins relational atoms from left to right. The leftmost
     * child operation should be passed into the first call, then the method creates the rest of
     * the joins recursively. The method pushes projection down the join tree where possible.
     *
     * @param atoms all unjoined atoms in query
     * @param leftChild left child operation for the join
     * @param comp list of join conditions
     * @param variables list of variables needed either for output or for join conditions
     * @return returns a join operator
     */
    private static JoinOperator createJoins(List<Operator> atoms, Operator leftChild, List<ComparisonAtom> comp,List<Variable> variables){
        Operator rightChild = atoms.get(0);
        JoinOperator newJoin = new JoinOperator(leftChild, rightChild, comp);

        List<Variable> neededVariables = new ArrayList<>(variables);

        // Push projection down the join tree if possible (to both child nodes)
        SumAggregate agg = plan.query.getHead().getSumAggregate();
        if (agg==null && newJoin.getCompareAtom()!=null){
            if (!neededVariables.contains((Variable) newJoin.getCompareAtom().getTerm1()))
                    neededVariables.add((Variable) newJoin.getCompareAtom().getTerm1());
            if (!neededVariables.contains((Variable) newJoin.getCompareAtom().getTerm2()))
                neededVariables.add((Variable) newJoin.getCompareAtom().getTerm2());

            Operator pushProjectionLeft = new ProjectOperator(leftChild, neededVariables,leftChild.getAtom());
            newJoin.setChildLeft(pushProjectionLeft);
            Operator pushProjectionRight = new ProjectOperator(rightChild, neededVariables,rightChild.getAtom());
            newJoin.setChildRight(pushProjectionRight);

            newJoin.reloadIndex();
        } else if (agg==null && newJoin.getCompareVar()!=null) {
            if (!neededVariables.contains(newJoin.getCompareVar())) neededVariables.add(newJoin.getCompareVar());

            Operator pushProjectionLeft = new ProjectOperator(leftChild, neededVariables,leftChild.getAtom());
            newJoin.setChildLeft(pushProjectionLeft);
            Operator pushProjectionRight = new ProjectOperator(rightChild, neededVariables,rightChild.getAtom());
            newJoin.setChildRight(pushProjectionRight);

            newJoin.reloadIndex();
        }

        // call method recursively if newJoin is not the last join
        if (atoms.size()==1){
            return newJoin;
        } else {
            atoms.remove(0);
            return createJoins(atoms, newJoin, comp, variables);
        }
    }

    /**
     * Getter for the root operator of the query plan
     *
     * @return root operator of query plan
     */
    public static Operator getRoot(){
        return plan.root;
    }
}
