package fr.esiee.devops.domain


case class Filter(key: String, value: Option[String], from: Option[String], to: Option[String])