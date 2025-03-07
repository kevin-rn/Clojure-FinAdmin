# Clojure Financial Administration
Simple webapp for purpose of Financial administration, built in Clojure. 
Contains only basic functionality of logging in/out, simple session management, adding and modifying transactions (invoices and expenses).

## Main tools
Backend: Clojure (Ring, Reitit)
Frontend: Hiccup + HTMX + TailwindCSS
Data Storage: PostgreSQL (docker)

## Project Structure

clojure-finadmin/  
│── deps.edn 					    # Project dependencies
|── docker-compose.yml				# PostgreSQL docker instance
|─── package.json
|    postcss.config.js				# TailwindCSS dependencies
|    tailwind.config.js
│── resources/  
| |─── database
| | |─── schema.sql					# Database schema
| |─── public						# Contains CSS styling, Javascript code and static images (icons, logos)
| |─── logback.xml 					# Logparsing settings
│── src/  
│ │─── finadmin/  
│ │ │─── core.clj 					# Entry point (Ring)  
│ │ │─── routes.clj 				# API and HTML routes (Reitit)  
│ │ │─── handlers.clj 				# Request handlers  
│ │ │─── db.clj 				    # PostgreSQL connection and queries
│ │ │─── views.clj 					# HTML rendering (Hiccup/HTMX)  
│── README.md





