package com.receitas.receitas.repository;

import com.receitas.receitas.entity.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findByStatus(String status);

    List<Receita> findByDataRegistroBetween(LocalDate inicio, LocalDate fim);

    List<Receita> findByStatusAndDataRegistroBetween(String status, LocalDate inicio, LocalDate fim);
}