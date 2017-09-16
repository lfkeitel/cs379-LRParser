# Example LR Parser

This is an implementation of a table-driven LR parser used in my programming languages class.
The grammar and state table were taken from Concepts of Programming Languages 11e by Robert W. Sebesta.

## Running

```shell
javac LRParser.java
java LRParser

# Optionally
java LRParser 'input'
```

## Grammar Rules

1. E -> E + T
2. E -> T
3. T -> T * F
4. T -> F
5. F -> ( E )
6. F -> id

## State Table

| State | id | +  | *  | (  | )   | $      |
|-------|----|----|----|----|-----|--------|
| 0     | S5 |    |    | S4 |     |        |
| 1     |    | S6 |    |    |     | accept |
| 2     |    | R2 | S7 |    | R2  | R2     |
| 3     |    | R4 | R4 |    | R4  | R4     |
| 4     | S5 |    |    | S4 |     |        |
| 5     |    | R6 | R6 |    | R6  | R6     |
| 6     | S5 |    |    | S4 |     |        |
| 7     | S5 |    |    | S4 |     |        |
| 8     |    | S6 |    |    | S11 |        |
| 9     |    | R1 | S7 |    | R1  | R1     |
| 10    |    | R3 | R3 |    | R3  | R3     |
| 11    |    | R5 | R5 |    | R5  | R5     |

## Goto Table

| State | E | T | F  |
|-------|---|---|----|
| 0     | 1 | 2 | 3  |
| 1     |   |   |    |
| 2     |   |   |    |
| 3     |   |   |    |
| 4     | 8 | 2 | 3  |
| 5     |   |   |    |
| 6     |   | 9 | 3  |
| 7     |   |   | 10 |
| 8     |   |   |    |
| 9     |   |   |    |
| 10    |   |   |    |
| 11    |   |   |    |