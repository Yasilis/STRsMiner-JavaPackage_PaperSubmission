<div id="top"></div>

<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![CCO-1.0 License][license-shield]][license-url]

<!-- # STRsMiner/TRsMiner -->

# Under Construction

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a>

  <h1 align="center">TRsMiner/STRsMiner</h1>

  <p align="center">
    A comprehensive scientific project in the field of bioinformatics for finding the relationship between TRs and TIS!
    <br />
    Using the enhanced query form on the Biomart Ensembl tool along with the RESTful API tools, a Java package was developed to retrieve, store, and analyze the data   and information.
    <br />
    <br />
    <br />
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details align="left">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#Project-Instructions">Project Instructions</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<p align="left">
    This project was originally developed to identify shor tandem repeats (STRs). But now, after a few years, all kinks of tandem repeats (TRs) can b identified.
    So, we decided to change the name from STRsMiner to TRsMiner.
    This project is developed in java and R language in the Intellij IDEA IDE.
    The Java language is used for core functionality and major activites. While R language is used for statistical calculations.
    This package has been developed to retrieve, store, and analyze the data and information in the field of TRs.
    For running this package you need to connect a database of TRs and other neccessary data.
    It is possible to use the database that has been created in the paper "Tandem repeats ubiquitously flank and contribute to translation initiation sites" which is candidate for publish (Not Published).
    The generated database has been used to analyze data and TRs during the study and it is ailable in the “figshare” repository, with the identifier “10. 0 /m .figshare.15 052 ”
</p>
<p align="left">
    Another way is to create a database with this package. In this way, you need to create a empty database in the Mysql and connect it to the package.
    It is possible to use any king of database. But you need to config hibernate.cfg.xml config file separately.
</p>
<p align="right">(<a href="#top">back to top</a>)</p>



### Built With
<p align="left">
You should download and install some frameworks,libraries, and tools to used, build and develop
the project. Here are a few examples.

* [git](https://git-scm.com/)
* [java](https://www.java.com/en/)
* [R](https://cran.r-project.org/)
* [MySQL](https://www.mysql.com/)
* [XAMPP](https://www.apachefriends.org/)(opptional for MySQL)
* [IDEA IDE](https://www.jetbrains.com/idea/)(opptional for development)
</p>
<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started
This is an example of how you may give instructions on setting up the project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

In the first step, you have to download clone of the project in your local environment.
You can do this by following command in the appropriate command line:

* clone the project
  ```sh
  git clone git@github.com:Yasilis/STRsMiner-JavaPackage_PaperSubmission.git
  ```

As you know, there are other ways to download source code. For example, you can use the Github user interface!

After downloading source codes, you need open the project by an IDE (I recommend you IntelliJ IDEA) and build it.
For building project you have to use the maven build script which has been attached in the project hierarchy.
The Hibernate ORM is also used in the project. So, you need config "hibernate.cfg.xml" file.
The default driver is "com.mysql.cj.jdbc.Driver" which it belongs to the MySQL database.
<br/>
<br/>
For changing default database you need to change this line of codes with appropriate Driver:
* In the "hibernate.cfg.xml" file
  ```sh
  <property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
  ```
In the next step you need to introduce your database in the project. It is possible according the following lines of code:

* In the "hibernate.cfg.xml" file
  ```sh
  <property name="connection.url">your DB address (jdbc:mysql://localhost:3306/STRsDataBase3?autoReconnect=true&amp;useSSL=false)</property>
  <property name="connection.username">your dbuser</property>
  <property name="connection.password">yout dbuserPassword</property>
  <property name="dialect">yout hibernate dialect  (org.hibernate.dialect.MySQLDialect)</property>
  ```
Pay attention that you have to change these settings with appropriate values in local machine.
Moreover, You also need to introduce at least one empty database into the "hibernate.cfg.xml" file.
There are multiple command in the project for creating tables and filling them.
<br/>
Also, you can also use our already prepared database which is accessible in the “figshare” repository, with the identifier “10. 0 /m .figshare.15 052 ”

After connecting database to the project, you have to build the project and export .jar file.
Another way is to use IDE directly. Because of the local setting that is necessary for running the package,
the prebuild .jar file is not implemented.
After making .jar file, you can enjoy package by following instructions.


### Project Instructions

Below is an example of how you can run package. TRsMiner/STRsMiner is a package consist of multiple tools and methods.
You can run each of them on your own as long as you follow the dependencies.
In other words, the required data at each method or tools must be provided to the program either through the database or through other methods.

In general for running the package and getting appropriate output, you need to run this general command in the console or command line;

* General command
  ```sh
  nohup java -jar TRsMiner1.0.0.3.jar input
  ```

In the above code, we have assumed that the name of .jar file is "TRsMiner1.0.0.3.jar".
the "nohup" keyword is used when we want to have a long runtime run. (So, for short runtime run you skip it)
In this case, the final output will be placed in "nohup.out" file beside of the "TRsMiner1.0.0.3.jar" file.
Also in this command, the "input" file is a text file that contains input information and executable commands for running the package.

The structure of "input" file is as follows:
* Input file:
  ```sh
  X 
  other instruction
  ```

In this format "X" is the number of command in the source code of project,
and other next lines are the inputs for that command.
In fact, in the main class of the project, we have a switch-case function that executes it runs different tools with different identifiers!

For example one input file
* Example 1 of input file:
  ```sh
  19
  7
  10
  80
  true
  ./report/@10,15@__MultiThread_GeneBased_simple_finger_Print_file.csv
  ./report/@-1,-1@__MultiThread_GeneBased_simple_finger_Print_file.csv
  ```

For example one input file
* Example 2 of input file:
  ```sh
  18
  ./assets/@16,120@__Bio_simple_finger_Print_file.csv
  ```

For example one input file
* Example 2 of input file:
  ```sh
  3
  40
  ```

According to the above examples, the most important package commands will be as follows:

0. Case 0: for calling help (Not yet fully developed!)
   ```sh
   0
   ```
1. Case 1: for printing all species in the DB
   ```sh
   1
   ```
2. Case 2: for updating species
   ```sh
   2
   ```
3. Case 3: for updating genes.
   ```sh
   3
   40
   ```
    1. second line is the number of threads which we want to use by package


4. Case 4: for updating genes belong to species id x.
   ```sh
   4
   7
   ```
    1. second line is an ID or index(x is an index of species is DB)


5. Case 5: for updating transcript.

   ```sh
   5
   120
   60
   ```
    1. second line is the length of Upstream sequence
    2. third line is the number of threads which we want to use by package


6. Case 8: for updating tandem repeats
   ```sh
   8
   70
   ```
    1. second line is the number of threads which we want to use by package

7. Case 17: for making a fingerprint file (one of the files for next analyzing)
   ```sh
   17
   ```
8. Case 67: for making a fingerprint file based on multi-threading
   and ***without gene based grouping***(one of the files for next analyzing)
   ```sh
   67
   84
   ```
    1. second line is the number of threads which we want to use by package

9. Case 68: for making a fingerprint file based on multi-threading
   and ***with gene based grouping***(one of the files for next analyzing)
   ```sh
   68
   80
   ```
    1. second line is the number of threads which we want to use by package


10. Case 19: for making a fingerprint file (one of the files for next analyzing)
    ```sh
    19
    7
    10
    80
    true
    ./report/@10,15@__MultiThread_GeneBased_simple_finger_Print_file.csv
    ./report/@-1,-1@__MultiThread_GeneBased_simple_finger_Print_file.csv
    ```
    1. second line is an ID or index(7 is index of human in our database )
    2. third line is the fold number of repeat process
    3. fourth line is the number of threads which we want to use by package
    4. fifth line is a switch for running analyzer gene based or not
    5. sixth line is the address of the fingerprint file for a specific category
    6. seventh line is the address of the fingerprint file for all specific categories




11. and so on ...



<p align="right">(<a href="#top">back to top</a>)</p>


<!-- USAGE EXAMPLES -->

## Usage

This project can be used for creating a database of tandem repeats and analyzing them.
Using the enhanced query form on the Biomart Ensembl tool along with the
RESTful API tools, a Java package was developed to retrieve, store, and analyze the data and information.
<p align="right">(<a href="#top">back to top</a>)</p>






<!-- CONTRIBUTING -->

## Contributing

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -am 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the Creative ***Commons Zero v1.0 Universal*** License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTACT -->

## Contact

Project Link: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission
<br/>
Email : ali.m.a.maddi@gmail.com
<br/>
Email : ali.maddi@ut.ac.ir

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/Yasilis/STRsMiner-JavaPackage_PaperSubmission.svg?style=for-the-badge
[contributors-url]: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission/graphs/contributors

[issues-shield]: https://img.shields.io/github/issues/Yasilis/STRsMiner-JavaPackage_PaperSubmission.svg?style=for-the-badge
[issues-url]: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission/issues

[license-shield]: https://img.shields.io/github/license/Yasilis/STRsMiner-JavaPackage_PaperSubmission.svg?style=for-the-badge
[license-url]: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission/blob/master/LICENSE

[forks-shield]: https://img.shields.io/github/forks/Yasilis/STRsMiner-JavaPackage_PaperSubmission.svg?style=for-the-badge
[forks-url]: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission/network/members

[stars-shield]: https://img.shields.io/github/stars/Yasilis/STRsMiner-JavaPackage_PaperSubmission.svg?style=for-the-badge
[stars-url]: https://github.com/Yasilis/STRsMiner-JavaPackage_PaperSubmission/stargazers




