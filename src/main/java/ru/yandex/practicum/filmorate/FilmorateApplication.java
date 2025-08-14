package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.h2.tools.Server;

import java.sql.SQLException;


@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) throws SQLException {
		Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
		SpringApplication.run(FilmorateApplication.class, args);
	}
}
