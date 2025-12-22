package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Cacheable(cacheNames ="dishCache",key = "#categoryId")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造查询redis的 key
//        String key = "dish_" + categoryId;
//        log.info("从redis缓存中查询菜品数据：{}",key);

        //查询redis有没有菜品
        List<DishVO> list = null;
//        try {
//            list = (List<DishVO>) redisTemplate.opsForValue()
//                    .get(key);
//        }catch (Exception e){
//            log.error("获取redis缓存数据失败：{}",e.getMessage());
//        }

        //判断菜品是否为空，如果不是空就直接返回菜品集合数据
//        if (list!=null && list.size()>0){
//            return Result.success(list);
//        }
        //如果是空，直接查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);

        //数据库查询完了之后，把数据存到redis缓存中
//        try {
//            redisTemplate.opsForValue().set(key,list);
//        }
//        catch (Exception e){
//            log.error("设置redis缓存失败：{}",e.getMessage());
//        }

        //返回结果
        return Result.success(list);
    }

}
