# Exam Scheduler with CSP


### Problem:
- Schedule all exams in the exam rooms
- Two exams should not be scheduled at the same time in the same exam room
- One exam takes exactly one term and is held in the required number of exam rooms
- Exams begin in one of the specified terms: 08:00, 11:30, 15:00, 18:30 and don't last longer than 3 hours
- Exams that require a computer can not be done in the rooms without them
- The number of students in the hall cannot exceed its capacity
- Exams from the same year of the same study program cannot be on the same day
- For each department, it is valid that two or more exams from neighboring years of study (first and second, second and third, third and fourth) that are offered in that department cannot be scheduled in one term.

### Problem in CSP:
- variables: exams
- domains: possible rooms and times
- constraints: the requirements we mentioned above

### Solver
Solver supports
- backtracking
- forward checking
- arc consistency

##### Backtracking
When the logic finds there is no consistent RoomAndTime to assign to a Exam, it removes the cause of assignment from scheduled subjects and put into subjects to scheduled and update other relevant parameters (available RoomsAndTimes, assigned RoomsAndTimes).

##### Forward checking
Before assigning a consistent RoomAndTime value to a Exam, the logic ensures that remaining Exams to be scheduled has at least one applicable RoomAndTime which is also availabe.

##### Arc consistency
This look-ahead method does more work at each instantiation than does forward-checking. If a variable's domain becomes empty during the process of enforcing arc-consistency, then the current candidate value is rejected.

### Heuristics

Priority is assigned to each Exam based on following heuristics:
- Minimum Remaining Values
Exam with the fewest number of applicable slots is chosen for next assignment.
- Least-Constraining Value








