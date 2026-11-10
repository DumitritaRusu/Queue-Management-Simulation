Queue Management Simulation

This is a Java application I developed for my Programming Techniques course. The project simulates how clients arrive and are assigned to different queues based on a selected strategy.
The main idea was to see how different queue management strategies affect waiting times and how clients are distributed between queues.
I worked on this project to better understand multithreading in Java and how to synchronize multiple threads.
It also helped me practice working with queues, implementing different strategies, and creating a graphical interface to visualize the simulation.

Features

Generate clients with random arrival and service times.
Simulate multiple queues running at the same time.
Choose between two queue management strategies:
Shortest Queue – assigns clients to the queue with the fewest clients.
Shortest Time – assigns clients to the queue with the shortest estimated waiting time.
Display the simulation in real time using a graphical interface.
Calculate the average waiting time, average service time and peak hour.
Save simulation results and queue states in a log file.

Technologies Used

Java

Java Swing for the graphical interface

Java Threads for running multiple queues

Maven

How It Works

The user can enter the number of clients and queues, the simulation duration, and the minimum and maximum arrival and service times.
After choosing a strategy and starting the simulation, clients are generated and assigned to queues as they arrive.
Each queue is handled by a separate thread, and the interface shows how the queues change throughout the simulation.
At the end, the application displays statistics about waiting times, service times and the peak hour.
