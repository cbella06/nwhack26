# Scheduler
*Made for [**NWHacks 2026**](https://devpost.com/software/aaa-0rg8hm) - 24-hour Hackathon*

A productivity website that will auto-generate a calendar based on the user's scheduled events, productivity, personalization settings, and task details.

## How to Run
1. Clone the repo
2. Ensure Java 17+ is installed.
3. Run the following command in the terminal: `./gradlew bootRun`
4. Access the app at [http://localhost:8080](http://localhost:8080)


## Tech Stack
* **Backend:** Spring Boot 3 (Java)
* **Frontend:** HTML5, CSS3, Thymeleaf
* **Database:** H2 (In-Memory)
* **Libraries:** ical4j (ICS Parsing)

## Key Features
 - **Smart Profiling**
   - Custom productivity peaks (morning, afternoon, evening, night) and working blocks
 - **ICS Integration** Import ICS file to include scheduled events (unavailable work times)
 - **Algorithmic Prioritization:** Creates a 7-day working schedule that prioritizes tasks by a weighted equation based on the following parameters
   - Due dates
   - Importance (1-5, with 5 being the most important)
   - Estimated time needed
   - Difficulty (Easy/Medium/Hard)
  - **Health-focused Scheduling**: Automated 45/15 Pomodoro-style work/break intervals.


## Scheduling Logic
We calculate **Task Priority** using the following formula:

$$Priority = (3 \times I) + \frac{10}{D + 1} + T + C$$

* **I:** Importance (1–5)
* **D:** Days until due
* **T:** Estimated time (Minutes)
* **C:** Complexity (1–5)

Each task is divided into 45-minute intervals. A 15-minute break follows each 45-minute interval to ensure productivity.
The divided times are assigned to available time slots determined by the user's indicated working time and scheduled events. The highest-priority task is scheduled earlier than lower-priority tasks. 
If a task has the same priority, then choose task order based on what is earliest due.

## Design Concept
Main Screen: The first screen when opening our web app. User can click "login" if they already have an account, and "sign up" if they are first time user. <br>
<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/64e7f8a9-f87f-4b56-83df-457caf399fbe" /><br>
Login page: User can login using their unique userid and password.<br>
<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/4e91411b-e007-4cb2-b7b6-7752b350fc96" /><br>
Sign up page: User make personalize set up to their account.<br>
<img width="500" height="800" alt="image" src="https://github.com/user-attachments/assets/7000557e-420d-47c8-86f3-9a50d8b5a25f" /><br>
Main Menu: Default menu page after login to user's account.<br>
<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/39065257-81bc-4f6d-b551-3c1ef7d03900" /><br>
Add New Task Page: Task making page that user can add in new tasks of the week. <br>
<img width="500" height="800" alt="image" src="https://github.com/user-attachments/assets/f143b237-6ee5-4596-be1e-91084979256e" /><br>
Weekly Calendar Page: Calendar page to show personalized planning of tasks of the week. User can swip left and right to check out daily plan from Sunday to Saturday. <br>
<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/21122ba5-a073-4526-b2f8-82cda92ce702" /><br>
Profile: User can edit personal setups to their account. <br>
<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/af82c012-658e-417e-9c4b-bcc0c99dfded" /><br>
