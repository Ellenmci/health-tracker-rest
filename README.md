## Overview

This project is a full‑stack health tracking application built with a Kotlin/Javalin backend and a Vue frontend.  
It supports CRUD operations for Users and Activities and includes three additional health‑related features: Heart Rate, Sleep, and Steps.  
The backend includes unit and integration tests, OpenAPI documentation, and a CI/CD pipeline.

## Core Features

- Users  
  - Create, read, update, delete  
  - Retrieve by ID or email  

- Activities  
  - Full CRUD  
  - Linked to users via foreign key  
  - Cascade delete on user removal  

## Additional Features

- Heart Rate  
  - Add and retrieve heart rate readings per user  
  - Summary endpoint (min, max, average BPM)

- Sleep  
  - Add, view, update, delete sleep entries  
  - Summary endpoint for total sleep duration  

- Steps  
  - Add, view, update, delete step entries  
  - Summary endpoint for total steps  

Each feature includes:
- A dedicated database table  
- DAO layer  
- Controller and routes  
- Vue UI components

  <img width="1676" height="578" alt="image" src="https://github.com/user-attachments/assets/9bb911ec-6be5-4683-961c-ceb512b15acf" />
I was hoping to have more screenshots of went it was working, but i lost them when i was rebooting my laptop.
