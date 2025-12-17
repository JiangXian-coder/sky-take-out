package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 保存菜品及其口味信息
     *
     * @param dishDTO 菜品传输对象，包含菜品基本信息和口味列表
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入菜品数据
        dishMapper.insert(dish);
        //获取插入的菜品的id
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();

        //批量插入口味数据
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品信息
     *
     * @param dishPageQueryDTO 菜品分页查询条件对象，包含页码、每页大小等查询参数
     * @return PageResult 分页查询结果对象，包含总记录数和当前页的数据列表
     */
    @Override
    public PageResult pagqQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 设置分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize());
        // 执行分页查询
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        // 构造并返回分页结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据菜品ID列表批量删除菜品
     *
     * @param ids 要删除的菜品ID列表
     * @throws DeletionNotAllowedException 当菜品处于起售状态或被套餐关联时抛出异常
     */
    @Override
    @Transactional
    public void deleteDishByIds(List<Long> ids) {
        //判断菜品是否处于起售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否处于套餐中
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(
                    MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //批量删除菜品和口味
        dishMapper.deleteBatchByIds(ids);
        dishFlavorMapper.deleteBatchByIds(ids);
    }



}
