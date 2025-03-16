# Clojure Financial Administration
Clojure-FinAdmin is a prototype web application designed for financial administration, built using Clojure. 
It offers basic functionalities such as user login/logout, session management, and the ability to add and modify transactions, currently only supporting simple invoices and expenses.

![login](https://github.com/user-attachments/assets/e73e982b-8106-4511-84cb-2646d2d49011)

## Technologies Used  
Backend: Clojure with Ring for HTTP handling, Reitit for routing.   
Frontend: Hiccup for templating, HTMX for dynamic content, TailwindCSS for styling, JavaScript and Chart.js for interactivity and charts.   
Data Storage: PostgreSQL for data storage, Docker for easy setup and deployment.  

## Project Structure
```  
clojure-finadmin/  
│─── deps.edn
|─── docker-compose.yml				  # PostgreSQL docker instance  
|─── package.json  
|─── postcss.config.js  				# TailwindCSS dependencies  
|─── tailwind.config.js  
│─── resources/    
| |─── database  
| | |─── schema.sql					# Database schema  
| |─── public						    # Contains CSS styling, Javascript code and static images (icons, logos)  
| |─── logback.xml 					# Logparsing settings  
│─── src/    
│ │─── finadmin/  
│ │ │─── database 					# PostgreSQL connection and queries   
│ │ │─── handlers 				  # Request handlers  
│ │ │─── views			        # HTML rendering (Hiccup/HTMX)
│ │ │─── routes.clj				  # API and HTML routes (Reitit)         
│ │ │─── core.clj					  # Entry point (Ring)       
│─── README.md   
```

## Prerequisites
Before setting up the project, ensure you have the following installed:
- Java: JDK 1.8 or higher.
- Clojure CLI: For managing Clojure dependencies and running the application.
- Docker: To run the PostgreSQL database in a containerized environment.
- Node.js and npm: For managing frontend dependencies and building assets.

Setup and Installation
1. Clone the Repository:  
    ```console
    foo@bar:~$ git clone https://github.com/kevin-rn/Clojure-FinAdmin.git  
    foo@bar:~$ cd Clojure-FinAdmin  
    ```
2. Set Up the PostgreSQL Database:  
   ```console
    foo@bar:~$ docker-compose up -d
   ```
3. Start the Application:  
   ```console
    foo@bar:~$ clj -M -m finadmin.core 
   ```
4. Accessing the Application:  
   Open a web browser and navigate to http://localhost:3000 to interact with the application.  
   
For tailwindcss development:
   ```console
    foo@bar:~$ npm install
   ```
And run either of the following:
   ```console
    foo@bar:~$ npm run build:css
    foo@bar:~$ npm run watch:css
   ```

## Features  
- Basic authentication with Ring session handling as well as ability to update password  
  ![update-password](https://github.com/user-attachments/assets/2d0cd612-7a50-4488-a7cf-2454c771678b)
  
- Responsive UI  
  ![sidebar](https://github.com/user-attachments/assets/410f3155-d2f9-450c-a470-36e7aa8c7014)
  
- Adding Transactions  
  ![add-transaction](https://github.com/user-attachments/assets/280aa40f-0726-4e50-ad86-ac0030491d3b)

- Viewing and interaction with Transaction History  
  ![transaction-history](https://github.com/user-attachments/assets/ec4d21fa-9e7c-4fdc-978a-cc717c88df04)

- Modifying Transactions (edit and delete)  
  ![modify-transaction](https://github.com/user-attachments/assets/ab08b591-3f07-4c64-b14f-3c5db1aad691)

## Limitations  
- Only supports simple Invoice and Expense transactions  
- Ability to actually upload documents is not in place  
  ![document-upload](https://github.com/user-attachments/assets/d8c8f387-e162-4a9b-969c-c241318bf0c7)
- Error message handling. Only a few errors and exceptions are handled but not all.  
- Proper data handling. For example loading data into plots could be done better.  




