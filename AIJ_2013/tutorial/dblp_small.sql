drop database if exists dblp_small;
create database dblp_small;
use dblp_small;

create table high_hindex (arg1 int(11) NOT NULL, PRIMARY KEY (arg1));
create table paper (arg1 int(11) NOT NULL, PRIMARY KEY (arg1));
create table author (arg1 int(11) NOT NULL, PRIMARY KEY (arg1));
create table author_paper (arg1 int(11) NOT NULL, arg2 int(11) NOT NULL, PRIMARY KEY (arg1,arg2));
create table paper_paper (arg1 int(11) NOT NULL, arg2 int(11) NOT NULL, PRIMARY KEY (arg1,arg2));

CREATE INDEX arg1 ON author (arg1);
CREATE INDEX arg1 ON paper (arg1);
CREATE INDEX arg1 ON paper_paper (arg1);
CREATE INDEX arg2 ON paper_paper (arg2);
CREATE INDEX arg1 ON author_paper (arg1);
CREATE INDEX arg2 ON author_paper (arg2);
INSERT INTO author VALUES(1);
INSERT INTO author VALUES(2);
INSERT INTO author VALUES(3);
INSERT INTO author VALUES(4);
INSERT INTO author VALUES(5);
INSERT INTO author VALUES(6);
INSERT INTO paper VALUES(1);
INSERT INTO paper VALUES(2);
INSERT INTO paper VALUES(3);
INSERT INTO paper VALUES(4);
INSERT INTO paper VALUES(5);
INSERT INTO paper VALUES(6);
INSERT INTO paper VALUES(7);
INSERT INTO paper VALUES(8);
INSERT INTO paper VALUES(9);
INSERT INTO paper VALUES(10);
INSERT INTO author_paper VALUES(1,1);
INSERT INTO author_paper VALUES(1,2);
INSERT INTO author_paper VALUES(1,3);
INSERT INTO author_paper VALUES(1,4);
INSERT INTO author_paper VALUES(1,5);
INSERT INTO author_paper VALUES(1,6);
INSERT INTO author_paper VALUES(2,1);
INSERT INTO author_paper VALUES(2,3);
INSERT INTO author_paper VALUES(2,8);
INSERT INTO author_paper VALUES(2,10);
INSERT INTO author_paper VALUES(3,3);
INSERT INTO author_paper VALUES(3,4);
INSERT INTO author_paper VALUES(3,5);
INSERT INTO author_paper VALUES(3,6);
INSERT INTO author_paper VALUES(3,8);
INSERT INTO author_paper VALUES(3,9);
INSERT INTO author_paper VALUES(3,10);
INSERT INTO author_paper VALUES(4,3);
INSERT INTO author_paper VALUES(4,7);
INSERT INTO author_paper VALUES(5,1);
INSERT INTO author_paper VALUES(5,4);
INSERT INTO author_paper VALUES(5,7);
INSERT INTO author_paper VALUES(5,8);
INSERT INTO author_paper VALUES(5,9);
INSERT INTO author_paper VALUES(5,10);
INSERT INTO author_paper VALUES(6,1);
INSERT INTO author_paper VALUES(6,2);
INSERT INTO author_paper VALUES(6,3);
INSERT INTO paper_paper VALUES(1,2);
INSERT INTO paper_paper VALUES(1,3);
INSERT INTO paper_paper VALUES(1,5);
INSERT INTO paper_paper VALUES(2,4);
INSERT INTO paper_paper VALUES(2,8);
INSERT INTO paper_paper VALUES(2,9);
INSERT INTO paper_paper VALUES(2,10);
INSERT INTO paper_paper VALUES(3,7);
INSERT INTO paper_paper VALUES(3,8);
INSERT INTO paper_paper VALUES(4,5);
INSERT INTO paper_paper VALUES(5,8);
INSERT INTO paper_paper VALUES(5,9);
INSERT INTO paper_paper VALUES(5,10);
INSERT INTO paper_paper VALUES(6,8);
INSERT INTO paper_paper VALUES(6,9);
INSERT INTO paper_paper VALUES(7,8);
INSERT INTO paper_paper VALUES(7,10);
INSERT INTO paper_paper VALUES(8,9);
INSERT INTO paper_paper VALUES(9,10);
INSERT INTO high_hindex VALUES(3);
INSERT INTO high_hindex VALUES(5);
