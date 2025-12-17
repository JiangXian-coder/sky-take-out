package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {
    public void saveWithFlavor(DishDTO dishDTO);

    PageResult pagqQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteDishByIds(List<Long> ids);
}
