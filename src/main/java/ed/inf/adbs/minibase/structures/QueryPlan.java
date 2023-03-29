package ed.inf.adbs.minibase.structures;


import ed.inf.adbs.minibase.base.*;

import java.io.FileNotFoundException;
import java.util.*;

public class QueryPlan {
    public static QueryPlan plan = null;
    Operator root;
    Query query;

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

        List<Variable> neededVariables = new ArrayList<>(query.getHead().getVariables());
        for (ComparisonAtom comp:comparisonAtomList){
            if (comp.getTerm1() instanceof Variable
                    && !neededVariables.contains((Variable) comp.getTerm1()))
                    neededVariables.add((Variable) comp.getTerm1());
            if (comp.getTerm2() instanceof Variable
                    && !neededVariables.contains((Variable) comp.getTerm2()))
                    neededVariables.add((Variable) comp.getTerm2());
        }

        plan.root = operators.get(0);
        if (operators.size()>1){
            operators.remove(0);
            plan.root = createJoins(operators,plan.root,joinComparators,neededVariables);
        }

        SumAggregate agg = query.getHead().getSumAggregate();
        if (agg != null){
            plan.root = new SumOperator(plan.root, agg,plan.root.getAtom(),query.getHead().getVariables());
        } else {
            plan.root = new ProjectOperator(plan.root, query.getHead().getVariables(), (plan.root).getAtom());
        }
    }

    public static boolean isJoinComparator(ComparisonAtom comp,List<Operator> operators){
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

    public static Operator createJoins(List<Operator> atoms, Operator leftChild, List<ComparisonAtom> comp,List<Variable> variables){
        Operator rightChild = atoms.get(0);
        JoinOperator newJoin = new JoinOperator(leftChild, rightChild, comp);

        // IF it has a compare-atom, it is safe to push projection with the variables list (has both head+comparators)
        // ELSE IF it has a compare variable, it is safe to push projection with the variable added
        SumAggregate agg = plan.query.getHead().getSumAggregate();
        if (agg==null && newJoin.getCompareAtom()!=null){
            Operator pushProjectionLeft = new ProjectOperator(leftChild, variables,leftChild.getAtom());
            newJoin.setChildLeft(pushProjectionLeft);
            Operator pushProjectionRight = new ProjectOperator(rightChild, variables,rightChild.getAtom());
            newJoin.setChildRight(pushProjectionRight);
            newJoin.reloadIndex();
        } else if (agg==null && newJoin.getCompareVar()!=null) {
            List<Variable> addVar = new ArrayList<>(variables);
            if (!addVar.contains(newJoin.getCompareVar())){
                addVar.add(newJoin.getCompareVar());
            }

            Operator pushProjectionLeft = new ProjectOperator(leftChild, addVar,leftChild.getAtom());
            newJoin.setChildLeft(pushProjectionLeft);
            Operator pushProjectionRight = new ProjectOperator(rightChild, addVar,rightChild.getAtom());
            newJoin.setChildRight(pushProjectionRight);
            newJoin.reloadIndex();
        }


        if (atoms.size()==1){
            return newJoin;
        } else {
            atoms.remove(0);
            return createJoins(atoms, newJoin, comp, variables);
        }
    }

    public static Operator getRoot(){
        return plan.root;
    }
}
