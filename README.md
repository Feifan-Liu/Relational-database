# Relational-database
Implementation of relational database with Java
- First part:
Constructed the basic building block of a table: Tuples, implemented a simple Catalog that keeps track of what tables are currently in the database and provides a way to access them. Implemented HeapFiles which are the physical representation of the data in our database.
- Second part:
Implemented relational operations, implemented the operations themselves then used JSQLparser to help translate SQL queries into these relational operations, effectively allowing users to query the data stored on the server.
- Third part:
Implemented B+ trees for indexing.
- Fourth part:
Implemented locking that can be used with transactions. The transactions will use the two phase locking routine. These transactions are specifically expected to implement strict two phase locking, meaning that they will acquire all locks before performing any modifications on data. Locks should generally be kept until the transaction is complete (either aborted or committed), though it may be possible to release some locks earlier than that.
