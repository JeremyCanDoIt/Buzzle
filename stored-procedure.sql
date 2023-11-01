DELIMITER $$
CREATE PROCEDURE add_service(
    IN title VARCHAR(100),
    IN `description` TEXT,
    IN price DECIMAL(13,4),
    IN posted_date DATE,
    IN `status` enum('OPEN','COMPLETED','IN-PROGRESS'),
    IN payment_type enum('HOURLY','WEEKLY','LUMP'),
    IN seller_username VARCHAR(50)
    )
BEGIN
	INSERT INTO Services(sellerID, title, `description`, price, posted_date, `status`, payment_type)
    SELECT sellerID, title, `description`, price, posted_date, `status`, payment_type
    FROM Seller s
    INNER JOIN User u ON u.id = s.sellerID
	WHERE u.username = seller_username
    LIMIT 1;
END$$
DELIMITER ;
