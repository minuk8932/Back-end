package com.example.backend.repositories;

import com.example.backend.models.ManualImages;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualImageRepository extends CrudRepository<ManualImages, Long> {
    @Query("SELECT mi FROM ManualImages AS mi, Recipes AS r WHERE r.id = ?1")
    public List<ManualImages> findAllByRecipeId(Long recipeId);
}
