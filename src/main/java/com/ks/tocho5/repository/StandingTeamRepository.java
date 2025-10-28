package com.ks.tocho5.repository;
import com.ks.tocho5.model.StandingTeamModel;

//Si status es enum, importa tu enum y cambia el tipo de parámetro
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StandingTeamRepository extends JpaRepository<StandingTeamModel,Integer> {

}
