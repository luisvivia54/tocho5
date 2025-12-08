package com.ks.tocho5.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import java.util.List;

public interface EquiposRepository extends JpaRepository<EquiposModel, Long>{
	 int countByCaptain(AppUser captain);
	 List<EquiposModel> findByCaptain(AppUser captain);
}