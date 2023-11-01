CREATE DATABASE IF NOT EXISTS servicesDB;
USE servicesDB;

CREATE TABLE IF NOT EXISTS GeneralLocation(
	locationID INT AUTO_INCREMENT,
	city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,

	PRIMARY KEY(locationID)
);

CREATE TABLE IF NOT EXISTS User (
    id INT AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    `password` VARCHAR(128) NOT NULL,
    displayName VARCHAR(50) NOT NULL,
    locationID INT NOT NULL,

    PRIMARY KEY(id),
    FOREIGN KEY(locationID) REFERENCES GeneralLocation(locationID)
);

CREATE TABLE IF NOT EXISTS Seller(
    sellerID INT,
    websiteLink VARCHAR(50), -- nullable
    profileDescription TEXT, -- nullable

	PRIMARY KEY(sellerID),
    FOREIGN KEY(sellerID) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Services(
    id INT AUTO_INCREMENT,
    sellerID INT NOT NULL,
    customerID INT, -- nullable; if there is no client yet
    title VARCHAR(100) NOT NULL,
    `description` TEXT, -- nullable
    price DECIMAL(13, 4) NOT NULL,
    posted_date DATE NOT NULL,
    `status` ENUM('OPEN','COMPLETED','IN-PROGRESS') NOT NULL,
    `payment_type` ENUM('HOURLY', 'WEEKLY', 'LUMP') NOT NULL,

    PRIMARY KEY(id),
    FOREIGN KEY(sellerID) REFERENCES Seller(sellerID),
    FOREIGN KEY(customerID) REFERENCES User(id)
);

CREATE TABLE IF NOT EXISTS Specialization(
    specID INT AUTO_INCREMENT,
    category VARCHAR(50) NOT NULL,

    PRIMARY KEY(specID)
);

CREATE TABLE IF NOT EXISTS SellerSpecialize(
    sellerID INT,
    specID INT,

    PRIMARY KEY(sellerID, specID),
    FOREIGN KEY(sellerID) REFERENCES Seller(sellerID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(specID) REFERENCES Specialization(specID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS CreditCard(
    cardID INT AUTO_INCREMENT,
    cardNum VARCHAR(20) NOT NULL,
	`name` VARCHAR(50) NOT NULL,
	expiration DATE NOT NULL,

    PRIMARY KEY(cardID)
); 

CREATE TABLE IF NOT EXISTS UserCreditCard(
    userID INT,
    cardID INT,

    PRIMARY KEY(userID, cardID),
    FOREIGN KEY(userID) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(cardID) REFERENCES CreditCard(cardID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS SellerReview(
    customerID INT NOT NULL,
    sellerID INT NOT NULL,
    rating FLOAT NOT NULL,
    `timestamp` datetime NOT NULL,
    title VARCHAR(50) NOT NULL,
    reviewBody TEXT, -- nullable; empty review

	PRIMARY KEY(customerID, sellerID),
    FOREIGN KEY(customerID) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(sellerID) REFERENCES Seller(sellerID) ON DELETE CASCADE ON UPDATE CASCADE
); 

CREATE TABLE IF NOT EXISTS Freelancer(
    freelancerID INT,
    `money` DECIMAL(13, 4) NOT NULL,

    PRIMARY KEY(freelancerID),
    FOREIGN KEY(freelancerID) REFERENCES Seller(sellerID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Company(
    companyID INT,
    address VARCHAR(50) NOT NULL,
    phoneNumber VARCHAR(20) NOT NULL,

    PRIMARY KEY(companyID),
    FOREIGN KEY(companyID) REFERENCES Seller(sellerID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Employee(
	email VARCHAR(50) PRIMARY KEY,
	password VARCHAR(128) NOT NULL,
	fullname VARCHAR(100)
);
