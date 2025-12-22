package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 保存菜品信息
     *
     * @param dishDTO 菜品数据传输对象，包含菜品基本信息和口味信息
     * @return 操作结果，成功时返回默认成功响应
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品......");
        // 保存菜品及其关联的口味信息
        dishService.saveWithFlavor(dishDTO);

        // 清理redis缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.info("清理redis缓存数据失败：{}", e.getMessage());
        }


        return Result.success();
    }

    /**
     * 分页查询菜品信息
     *
     * @param dishPageQueryDTO 菜品分页查询条件对象，包含页码、每页条数、菜品名称、分类ID等查询参数
     * @return 返回分页查询结果，包含菜品列表和总记录数
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品信息:{}", dishPageQueryDTO);
        // 执行菜品分页查询业务逻辑
        PageResult pageResult = dishService.dishPageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     *
     * @param ids 要删除的菜品ID列表
     * @return 操作结果，成功时返回成功状态
     */
    @DeleteMapping
    public Result deleteDishByIds(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);
        dishService.deleteDishByIds(ids);
        // 清理redis缓存数据
        clearRedisCache();


        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("根据ID查询菜品信息:{}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result updateDishWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品信息:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 清理redis缓存数据
        clearRedisCache();
        return Result.success();
    }


    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void clearRedisCache() {
        try {
            Set keys = redisTemplate.keys("dish_*");
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.info("清理redis缓存数据失败：{}", e.getMessage());
        }
    }
}
