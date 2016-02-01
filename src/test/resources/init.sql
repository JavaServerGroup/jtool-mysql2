CREATE TABLE IF NOT EXISTS `users` (`id` INT NOT NULL AUTO_INCREMENT,`name` VARCHAR(45) NOT NULL,`age` INT NOT NULL, PRIMARY KEY (`id`));
TRUNCATE TABLE users;
INSERT INTO `users` (`id`, `name`, `age`) VALUES ('1', 'jialechan', '8');
INSERT INTO `users` (`id`, `name`, `age`) VALUES ('2', 'KKL', '18');
INSERT INTO `users` (`id`, `name`, `age`) VALUES ('3', 'Ken', '28');