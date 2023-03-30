package ed.inf.adbs.minibase.minimizer;

import ed.inf.adbs.minibase.base.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Class to separate the methods used by CQMinimizer
 */
public class MinimizerUtils {
    /**
     * Method for checking whether removing the given atom from the query results
     * in a homomorphism.
     *
     * @param query original query
     * @param atom atom that will be attempted to remove
     * @return if removing the atom results in a valid query homomorphism, the
     * method returns true
     */
    public static boolean homomorphism(Query query, RelationalAtom atom) {
        // Make a copy of the query and remove the atom
        List<Atom> removeAtom = new ArrayList<>(query.getBody());
        removeAtom.remove(atom);

        // Makes a list of all terms in the query
        List<Term> mapTo = new ArrayList<>();
        for (Atom a:removeAtom){
            String thisAtomName = atom.getName();
            String loopAtomName = ((RelationalAtom) a).getName();

            if (thisAtomName.equals(loopAtomName)){
                mapTo.addAll(((RelationalAtom) a).getTerms());
            }
        }

        // Makes a list of all variables in the atom
        List<Variable> atomVars = new ArrayList<>();
        for (Term term:atom.getTerms()) {
            if ((term instanceof Variable) && !headContains(query,(Variable) term)){
                atomVars.add((Variable) term);
            }
        }

        // Store all possible mappings from a variable in the atom
        HashMap<Variable, Set<Term>> allMappings = new HashMap<>();
        for (Variable var:atomVars){
            Set<Term> copyMapTo = new HashSet<>();
            for (Term term:mapTo){
                if (!var.equals(term)){
                    copyMapTo.add(term);
                }
            }
            allMappings.put(var,copyMapTo);
        }

        // Transform to RelationalAtom list instead of Atom list
        List<RelationalAtom> raQuery = (query.getBody()).stream().map(a ->(RelationalAtom) a).collect(Collectors.toList());
        List<RelationalAtom> raRemoveAtom = removeAtom.stream().map(a ->(RelationalAtom) a).collect(Collectors.toList());

        // Iterate over all variable mappings
        return backtracker(raQuery,allMappings,raRemoveAtom);
    }

    /**
     * Inner class used in the backtracking
     */
    private static class BacktrackQueueElement{
        List<RelationalAtom> query;
        HashMap<Variable, Set<Term>> allMappings;
        public BacktrackQueueElement(List<RelationalAtom> query,HashMap<Variable, Set<Term>> allMappings){
            this.allMappings = allMappings;
            this.query = query;
        }
    }

    /**
     * Method for iterating through all possible mappings to check for a query homomorphism.
     *
     * @param query the original query without atoms removed
     * @param allMappings a Hashmap where the key is a variable and the value is a list of
     *                    possible mappings for that variable (constants or variables)
     * @param newQuery query with a single atom removed
     * @return returns true when a query homomorphism is found, or false if there is no
     * possible mapping in allMappings that makes a valid query homomorphism
     */
    private static boolean backtracker(List<RelationalAtom> query, HashMap<Variable, Set<Term>> allMappings, List<RelationalAtom> newQuery){

        if (equalQueries(query,newQuery)) return true;
        if(allMappings.isEmpty()) return false;

        // Generating every mapping and adding them to a queue in the form of a List of BacktrackQueueElement's
        List<BacktrackQueueElement> attempts = new ArrayList<>();
        for (Map.Entry<Variable, Set<Term>> fromMap : allMappings.entrySet()) {
            Variable var = fromMap.getKey();
            Set<Term> terms = fromMap.getValue();
            HashMap<Variable, Set<Term>> removeVar = new HashMap<>(allMappings);
            removeVar.remove(var);

            for (Term term : terms) {
                attempts.add(new BacktrackQueueElement( mapper(query, term, var), removeVar));
            }
        }

        // Iterating through queue
        for (BacktrackQueueElement fromQueue:attempts){
            if (backtracker(fromQueue.query, fromQueue.allMappings, newQuery)) return true;
        }
        return false;
    }

    /**
     * Method for mapping a variable into another term for an entire query.
     *
     * @param query original query
     * @param term term to replace the variable with
     * @param var variable to replace
     * @return returns the query with the given variable replaced with the term
     */
    private static List<RelationalAtom> mapper(List<RelationalAtom> query, Term term, Variable var) {
        List<RelationalAtom> newAtoms = new ArrayList<>();

        // iterate through every atom in the query and add to the newAtoms query
        for (RelationalAtom atom:query){
            String name = atom.getName();
            List<Term> terms = new ArrayList<>();

            // for every term, if replace it if the term equals the input variable, otherwise add the original term
            for (Term t: atom.getTerms()){
                if ((t instanceof Variable) && t.equals(var)){
                    terms.add(term);
                } else {
                    terms.add(t);
                }
            }

            newAtoms.add(new RelationalAtom(name,terms));
        }

        return newAtoms;
    }


    /**
     * Method for checking whether two queries are equal. Two queries are equal if every
     * atom in query A can be paired up with an identical atom in queryB, under set semantics.
     *
     * @param queryA first query
     * @param queryB second query
     * @return returns true if the queries are equal under set semantics
     */
    private static boolean equalQueries(List<RelationalAtom> queryA,List<RelationalAtom> queryB){

        List<RelationalAtom> unchecked = new ArrayList<>(queryB);

        for (RelationalAtom atomA:queryA){
            boolean hasMatch = false;

            for (RelationalAtom atomB:unchecked){
                if ((atomA.getName()).equals(atomB.getName()) && checkTerms(atomA.getTerms(),atomB.getTerms())){
                    unchecked.remove(atomB);
                    hasMatch = true;
                    break;
                }
            }

            if (!hasMatch){
                for (RelationalAtom atomB:queryB){
                    if ((atomA.getName()).equals(atomB.getName()) && checkTerms(atomA.getTerms(),atomB.getTerms())){
                        hasMatch = true;
                    }
                }
            }

            if (!hasMatch) return false;
        }

        // if there are "unchecked" queries it means there are atoms in queryB that are not in queryA
        return unchecked.size() == 0;
    }

    /**
     * Method for checking whether two lists of terms are equal.
     *
     * @param termsA first list of terms
     * @param termsB second list of terms
     * @return returns true if the terms at each index i are equal
     */
    private static boolean checkTerms(List<Term> termsA, List<Term> termsB){
        if (termsA.size()!=termsB.size()) return false;

        for (int i = 0; i<termsA.size();i++){
            if (!(termsA.get(i)).getClass().equals((termsB.get(i)).getClass())) return false;
            if(!(termsA.get(i)).equals(termsB.get(i))) return false;
        }
        return true;
    }


    /**
     * Method for checking whether the header contains a variable
     *
     * @param query original query
     * @param var variable to check for
     * @return returns a boolean indicating whether the variable is contained
     * in the header
     */
    private static boolean headContains(Query query,Variable var){
        return query.getHead().getVariables().contains(var);
    }
}
