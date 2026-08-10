These three files (`ConferenceData.java`, `ConferenceStreams2.java`, `RDFGenerator.java`) were moved here from
`src/main/java/utils/` on 2026-07-24 while fixing the Java 17 build.

They are confirmed dead code: nothing anywhere else in the codebase imports or references them (checked via a
full-repo grep for their class names). They also do not compile as-is — they were left mid-migration from an
earlier package (`genact.temporal.data.generator`) into `utils`, reaching into package-private fields on
`DataGenerator` that are no longer visible from a different package, and using a mix of old and new Jena RDF
APIs incompatible with each other.

The working, compiling equivalent of this logic already exists in `src/main/java/genact/temporal/data/generator/`
(e.g. `ConferenceStreams.java`, `DataGenerator.java`).

Moved out of `src/main/java` rather than deleted, in case there's logic here worth salvaging — delete this
directory once confirmed unneeded.
