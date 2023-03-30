


------------------------------------ TASK 2: JOIN LOGIC ------------------------------------
This is an explanation of how the join conditions are extracted and applied. 

The first step in the query planner is to iterate through all atoms in the query. At this 
stage ComparisonAtoms and RelationalAtoms are added to separate lists.

After this, join and select comparisons are separated into two lists by iterating over the 
list of ComparisonAtoms and using the isJoinComparator method to check whether the atom is
a selection or join condition. The isJoinComparator returns false (i.e. the atom is a select
condition) if one of the terms is a Constant OR both of the terms exist in the same relation 
(which is checked by iterating through the base relational atoms). The method returns true 
otherwise (i.e. the atom is a join condition).

Next the joins are created by calling the createJoins method recursively (which builds from left 
to right as instructed in the coursework specification). When a new join is created, all 
joinComparators are passed into the constructor. Then the constructor calls the parseComparisons 
function which locates the join condition for that join. 

parseComparisons decides on the join condition by iterating through the joinComparators. If one
of the terms in the ComparisonAtom belongs to the left child and the other term to the right
child, then the ComparisonAtom is set as the condition for that join. If parseComparisons finds
no ComparisonAtom, it tries to find a matching Variable in the two child operators instead.

The match() method in getNextTuple checks whether two tuples should be joined. It uses 
whichever condition (out of the ComparisonAtom or Variable that is) is not null to evaluate
whether the tuples are joined. If there is neither a ComparisonAtom or a Variable to match on,
the method always returns true (i.e. cartesian product).


----------------------------------- TASK 3: OPTIMIZATION ------------------------------------
There are a few optimisations applied in the query planner. The first is that all selection 
operations happen before any joins, which reduces the intermediate result significantly by
removing tuples that wont be in the output anyway.

Secondly, the query planner implements some projection pushing down the operations tree. First
the operation tree is generated in the order scans->selections->joins. Then the pushProjection
method is called on the root Operation. This method pushes down the projection into both child
nodes if it's a join operation, or just returns the original operation otherwise. After the
projections have been pushed down the tree, then the final projection is done, and the tree 
has been built.

pushProjection pushes the projection down through recursion. It starts by adding a projection
between the root of the tree and each of the roots child nodes. In this next projection the 
pushProjection method is called recursively such that the child is the root. The pushProjection
method essentially inserts a projection before every join. By removing unnecessary columns in
the amount of tuples in the intermediate result will get smaller, because any duplicates are 
removed in the projection.

Every time a projection is pushed down, the variables needed for the parent operator are added
to the projection variables. At each projection the variables projected are therefore:
    [variables in head + variables needed for all joins above the projection in the tree]

If the join is a cartesian product, then no more variables are added to the list of variables 
to keep. Hence, the cartesian product is only done on the columns that are in the output (or
in a join closer to the root) and is much more optimized than if it was done on all the columns.

Projections are not pushed down the tree if it contains a SUM operation. This is because 
projection does not preserve duplicates, hence the sum operation may return the wrong answer.




----------------------------- WRITING .dump() TO FILE VS STDOUT -----------------------------
This is just a note about the output.

As long as the WriteCSV singleton is not null when .dump() is called, the function writes the 
results to the specified output file, otherwise the function prints to standard output. In the 
submitted files, WriteCSV has been initialised before dump() and is closed afterwards, hence 
results should only be written to file.