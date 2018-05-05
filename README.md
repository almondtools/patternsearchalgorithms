PatternSearchAlgorithms
=======================
[![Build Status](https://api.travis-ci.org/almondtools/patternsearchalgorithms.svg)](https://travis-ci.org/almondtools/patternsearchalgorithms)
[![codecov](https://codecov.io/gh/almondtools/patternsearchalgorithms/branch/master/graph/badge.svg)](https://codecov.io/gh/almondtools/patternsearchalgorithms)

PatternSearchAlgorithms is a library using a deterministic finite automaton (**DFA**) for matching and searching. It does not provide features as group capturing, matching modes or backreferences. It is just efficient for complex patterns and large texts. The match time of such an automaton is linear dependent on the number of chars to match (**O(n)**, where n = number of chars to match).

Common regex packages use nondeterministic automatons (**NFA**) to capture the regular expression, allowing several special features but implying performance leaks. The match time of an NFA based solution is dependent on the nodes in the automaton and the chars to match (**O(m^2*n**), where m = number of automaton nodes, n = number of chars to match).

Matching a pattern
------------------
```Java
    Pattern pattern = Pattern.compile("(more|less) efficient", OptimizationTarget.MATCH);

    Matcher matcher = pattern.matcher("more efficient");
    System.out.prinltn("matches: " + matcher.matches()); // matches:true
    
    ...
    
    Matcher matcher = pattern.matcher("patternsearchalgorithms is more efficient than java.util.regex");
    System.out.prinltn("matches: " + matcher.matches()); // matches:false
```

Finding a pattern
-----------------
```Java
    Pattern pattern = Pattern.compile("(more|less) efficient", OptimizationTarget.SEARCH);

    Finder matcher = pattern.matcher("patternsearchalgorithms is more efficient than java.util.regex");
    while (matcher.find()) {
        System.out.println("found text = " + matcher.match.text()); // found text = more efficient
        System.out.println("at = " + matcher.match.start()); // at = 27
        System.out.println("to = " + matcher.match.end()); // to = 14
    }

    ...
    
    Finder matcher = pattern.matcher("patternsearchalgorithms is more efficient than java.util.regex");
    for (Match match : matcher.findAll()) {
        System.out.println("found text = " + match.text()); // found text = more efficient
        System.out.println("at = " + match.start()); // at = 27
        System.out.println("to = " + match.end()); // to = 14
    }
```

Syntax
======
We support the regular expression syntax of [regexparser](https://github.com/almondtools/regexparser).

NFA-Expressions (java.util.Pattern) vs. DFA-Expressions
=======================================================
Java regular expressions (java.util.Pattern) are quickly created and optimized. Simple regular expressions are executed quite fast.

PatternSearchAlgorithms regular expressions need a long creation and optimization time. After this initial effort the execution time is no longer dependent on pattern complexity.

Use Java regular expressions:
- if the expression is short and simple
- if the expression is matched only a few times
- if the expression is often created (e.g. in a loop)

Use patternsearchalgorithms regular expressions:
- if the expression is long or complex
- if the expression is matched many times
- if the expression is once created and often applied


Performance Comparison
======================
A performance benchmark for regex packages can be found at https://github.com/almondtools/regexbench.

This benchmark does not only check the performance but also the correctness of the results:
- each benchmark fails if the expected number matches is not found
- DFA packages cannot compute the same groups as NFA packages - accepted difference
- Non-Posix-NFA packages (as jregex and java.util.regex) do not always detect the longest leftmost match - accepted difference

Using PatternSearchAlgorithms
===============================

Maven Dependency
----------------

```xml
<dependency>
    <groupId>net.amygdalum</groupId>
    <artifactId>patternsearchalgorithms</artifactId>
    <version>0.0.3</version>
</dependency>
```