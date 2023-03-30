package ed.inf.adbs.minibase.utils;

import ed.inf.adbs.minibase.base.ComparisonOperator;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

/**
 * Utility class for helping evaluate selection and join conditions
 */
public class CompareUtil {

    /**
     * Evaluates the condition using a switch statement on the comparison operator.
     *
     * @param termA term on the left-hand side of the comparison operator
     * @param termB term on the right-hand side of the comparison operator
     * @param cop comparison operator
     * @return returns the boolean result of the comparison evaluation
     */
    public static boolean evaluateComparison(Constant termA, Constant termB, ComparisonOperator cop){
        if (termA.getClass()!=termB.getClass()) return false;

        int comparisonResult;
        if (termA.getClass().equals(IntegerConstant.class)) {
            comparisonResult = ((IntegerConstant) termA).compareTo((IntegerConstant) termB);
        } else if (termA.getClass().equals(StringConstant.class)) {
            comparisonResult = ((StringConstant) termA).compareTo((StringConstant) termB);
        } else {
            throw new IllegalArgumentException("");
        }

        switch(cop) {
            case EQ:
                return comparisonResult==0;
            case NEQ:
                return comparisonResult!=0;
            case GT:
                return comparisonResult>0;
            case GEQ:
                return comparisonResult>=0;
            case LT:
                return comparisonResult<0;
            case LEQ:
                return comparisonResult<=0;
            default:
                throw new IllegalArgumentException("Unrecognized comparison operator");
        }
    }

    /**
     * Swaps the condition operation.
     *
     * @param cop original condition operation
     * @return returns swapped condition operation (if applicable)
     */
    public static ComparisonOperator swapCompare(ComparisonOperator cop){
        switch(cop) {
            case EQ:
            case NEQ:
                return cop;
            case GT:
                return ComparisonOperator.LT;
            case GEQ:
                return ComparisonOperator.LEQ;
            case LT:
                return ComparisonOperator.GT;
            case LEQ:
                return ComparisonOperator.GEQ;
            default:
                throw new IllegalArgumentException("Unrecognized comparison operator");
        }
    }
}
