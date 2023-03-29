package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.*;
import java.io.FileWriter;
import java.io.File;
import java.util.stream.Collectors;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        parsingExample(inputFile);

        minimizeCQ(inputFile, outputFile);
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        try {
            Query query = QueryParser.parse(Paths.get(inputFile));

            // Minimize
            minimize(query);

            // Write to file
            try{
                File file = Paths.get(outputFile).toFile();
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                System.out.println("Output: " +query.toString());
                writer.write(query.toString());
                writer.close();
            } catch (Exception e) {
                System.err.println("Exception occurred while writing to file.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    public static void minimize(Query query) {
        // Q(x1,…,xk) :- body
        // Repeat until no change
        boolean change = true;
        while(change){
            change = false;
            for (Atom atom: query.getBody()){
                //System.out.println("------------------ Atom " + atom + "------------------");
                //choose an atom α ∈ body such that the variables x1,…,xk appear in body ∖ {α}
                if (inBody(query,(RelationalAtom) atom)){
                    //if there is a query homomorphism from Q(x1,…,xk) :- body to Q(x1,…,xk) :- body ∖ {α}
                    if (homomorphism(query,(RelationalAtom) atom)){
                        System.out.println("Homomorphism found!");
                        //then body := body ∖ {α}
                        query.getBody().remove(atom);
                        change = true;
                        break;
                    }
                }
            }
        }
    }

    public static boolean inBody(Query query, RelationalAtom atom) {
        //TODO
        return true;
    }

    public static boolean homomorphism(Query query, RelationalAtom atom) {
        List<Atom> removeAtom = new ArrayList<>(query.getBody());
        removeAtom.remove(atom);

        //System.out.println("homomorphism: Query = " + query);
        //System.out.println("homomorphism: removeAtom = " + removeAtom);


        List<Term> mapTo = new ArrayList<>();

        for (Atom a:removeAtom){
            String thisAtomName = atom.getName();
            String loopAtomName = ((RelationalAtom) a).getName();

            if (thisAtomName.equals(loopAtomName)){
                mapTo.addAll(((RelationalAtom) a).getTerms());
            }
        }

        List<Variable> atomVars = new ArrayList<>();
        for (Term term:atom.getTerms()) {
            if ((term instanceof Variable) && !headContains(query,(Variable) term)){ // && ensures casting should only happen if the term is a variable
                atomVars.add((Variable) term);
            }
        }

        HashMap<Variable, Set<Term>> allMappings = new HashMap<>();

        for (Variable var:atomVars){
            Set<Term> copyMapTo = new HashSet<>();

            // filtering out the same variable from the list of terms
            // don't want to call mapTo.remove() because we need it in the next iterations
            for (Term term:mapTo){
                if (!var.equals(term)){
                    copyMapTo.add(term);
                }
            }
            allMappings.put(var,copyMapTo);
        }

        // transform to RelationalAtom list instead of Atom list
        List<RelationalAtom> raQuery = (query.getBody()).stream().map(a ->(RelationalAtom) a).collect(Collectors.toList());
        List<RelationalAtom> raRemoveAtom = removeAtom.stream().map(a ->(RelationalAtom) a).collect(Collectors.toList());

        return backtracker(raQuery,allMappings,raRemoveAtom);
    }

    static class BacktrackQueueElement{
        protected List<RelationalAtom> query;
        protected HashMap<Variable, Set<Term>> allMappings;
        BacktrackQueueElement(List<RelationalAtom> query,HashMap<Variable, Set<Term>> allMappings){
            this.allMappings = allMappings;
            this.query = query;
        }
    }

    private static boolean checkTerms(List<Term> termsA, List<Term> termsB){
        if (termsA.size()!=termsB.size()){
            return false;
        }

        for (int i = 0; i<termsA.size();i++){
            //System.out.println(termsA.get(i) + " == " + termsB.get(i) + " = " + ((termsA.get(i)).equals(termsB.get(i))));
            if (!(termsA.get(i)).getClass().equals((termsB.get(i)).getClass())) return false;
            if(!(termsA.get(i)).equals(termsB.get(i))) return false;
        }
        return true;
    }

    // for checking if the queries are equal
    private static boolean equalQueries(List<RelationalAtom> queryA,List<RelationalAtom> queryB){
        // to ensure there are no "extras" in the minimized query
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

            if (!hasMatch){
                System.out.println("Equal? A:" + queryA + ", B:"+ queryB + ", false");
                return false;
            }
        }

        //System.out.println("Equal? A:" + queryA + ", B:"+ queryB + ", true");
        return true;
    }

    private static boolean backtracker(List<RelationalAtom> query, HashMap<Variable, Set<Term>> allMappings, List<RelationalAtom> newQuery){
        // if the sets are equal we have a homomorphism
        // (if this happens on first iteration, the removed atom was equal to another one in the query)
        if (equalQueries(query,newQuery)){
            return true;
        }

        // if there are no mappings => false
        if(allMappings.isEmpty()){
            return false;
        }

        List<BacktrackQueueElement> attempts = new ArrayList<>();

        // mapping every possible homomorphism mapping
        for (Map.Entry<Variable, Set<Term>> fromMap : allMappings.entrySet()) {
            Variable var = fromMap.getKey();
            Set<Term> terms = fromMap.getValue();

            HashMap<Variable, Set<Term>> removeVar = new HashMap<>(allMappings);
            removeVar.remove(var);

            Iterator<Term> termsIterator = terms.iterator();
            while(termsIterator.hasNext()) {
                attempts.add(new BacktrackQueueElement(mapper(query, termsIterator.next(), var),removeVar));
            }
        }

        for (BacktrackQueueElement fromQueue:attempts){
            if (backtracker(fromQueue.query, fromQueue.allMappings, newQuery)){
                return true;
            }
        }

        return false;
    }


    private static List<RelationalAtom> mapper(List<RelationalAtom> query, Term term, Variable var) {
        List<RelationalAtom> newAtoms = new ArrayList<>();
        for (RelationalAtom atom:query){
            String name = atom.getName();
            List<Term> terms = new ArrayList<>();
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

    private static boolean headContains(Query query,Variable var){
        return query.getHead().getVariables().contains(var);
    }

    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */
    public static void parsingExample(String filename) {

        try {
            Query query = QueryParser.parse(Paths.get(filename));
            // Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
            // Query query = QueryParser.parse("Q(x) :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
