package com.example.backend.services;

import com.example.backend.configurations.RDAConfig;
import com.example.backend.models.Recipes;
import com.example.backend.models.data_enums.RDA;
import com.example.backend.repositories.ManualImageRepository;
import com.example.backend.repositories.ManualRepository;
import com.example.backend.repositories.RecipeRepository;
import com.example.backend.services.interfaces.RecipeService;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final ManualRepository manualRepository;
    private final ManualImageRepository manualImageRepository;

    private final RDAConfig rdaConfig;

    public RecipeServiceImpl(RecipeRepository recipeRepository, ManualRepository manualRepository, ManualImageRepository manualImageRepository, RDAConfig rdaConfig) {
        this.recipeRepository = recipeRepository;
        this.manualRepository = manualRepository;
        this.manualImageRepository = manualImageRepository;
        this.rdaConfig = rdaConfig;
    }

    @Override
    public List<Recipes> recipeListExtractFromDB() {
        List<Recipes> recipeDB = new ArrayList<>();
        recipeDB.addAll((Collection<? extends Recipes>) recipeRepository.findAll());

        return recipeDB;
    }

    @Override
    public List<Optional> termFrequencyInverseDocumentFrequency(double[] ingested, List<Recipes> recipesArrayList, double responseSize) {
        RDA rda = rdaConfig.getRecommendedDailyAllowance();
        double car = rda.getCarbohydrate() * 2 - ingested[0];
        double pro = rda.getProtein() * 2 - ingested[1];
        double fat = rda.getFat() * 2 - ingested[2];

        double[] sum = new double[3];
        double[] max = new double[3];
        int size = recipesArrayList.size();

        for(Recipes recipe: recipesArrayList) {
            sum[0] = recipe.getCarbohydrate();
            sum[1] = recipe.getProtein();
            sum[2] = recipe.getFat();

            for(int index = 0; index < sum.length; index++) {
                max[index] = Math.max(max[index], sum[index]);
            }
        }

        double avg = (sum[0] + sum[1] + sum[2]) / size;
        double nutrientsMax = max[0] + max[1] + max[2];
        double current = car + pro + fat;

        double user = Math.log(current / avg) * (0.5 + (0.5 * current / nutrientsMax));

        PriorityQueue<AbstractMap.SimpleEntry<Long, Double>> tfidf = new PriorityQueue<>((f1, f2) ->
                f1.getValue() < f2.getValue() ? -1: 1);

        for(Recipes recipe: recipesArrayList) {
            if(recipe.getCarbohydrate() > car || recipe.getProtein() > pro || recipe.getFat() > fat) continue;
            current = recipe.getCarbohydrate() + recipe.getProtein() + recipe.getFat();

            tfidf.offer(new AbstractMap.SimpleEntry<>(recipe.getId()
                    , Math.abs(user - Math.log(current / avg) * (0.5 + (0.5 * current / nutrientsMax)))));
        }

        List<Optional> result = new ArrayList<>();

        while(responseSize-- > 0) {
            long target = tfidf.poll().getKey();

            result.add(recipeRepository.findById(target));
            result.add(manualRepository.findById(target));
            result.add(manualImageRepository.findById(target));
        }

        return result;
    }

    @Override
    public void save(Recipes recipe) {

    }

    @Override
    public void delete(Recipes recipe) {

    }
}
