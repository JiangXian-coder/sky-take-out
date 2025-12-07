package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        //拷贝employeeDTO数据到employee的Entity中
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置默认账号状态(1表示启用,0表示禁用)
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置创建时间
//        employee.setCreateTime(LocalDateTime.now());
//        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人ID
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.add(employee);
    }

        /**
     * 员工分页查询方法
     *
     * @param employeePageQueryDTO 员工分页查询条件数据传输对象，包含页码、每页大小等查询参数
     * @return PageResult 分页查询结果对象，包含总记录数和当前页的数据列表
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 设置分页参数
        PageHelper.startPage(employeePageQueryDTO.getPage(),
                employeePageQueryDTO.getPageSize());
        log.info("员工分页查询开始......");
        // 执行分页查询
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> result = page.getResult();
        // 封装分页结果
        return new PageResult(total, result);
    }


        /**
     * 更新员工状态
     *
     * @param id 员工ID
     * @param status 员工状态
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        // 构建员工对象并设置更新时间和状态
        Employee employee = Employee.builder().status(status).id(id)
                .build();

        // 执行更新操作
        employeeMapper.update(employee);
    }


        /**
     * 根据员工ID获取员工信息
     *
     * @param id 员工ID
     * @return 员工对象，其中密码字段被替换为掩码显示
     */
    @Override
    public Employee getById(Long id) {
        // 查询员工信息
        Employee employee = employeeMapper.getById(id);
        // 隐藏密码信息
        employee.setPassword("****");
        return employee;
    }


        /**
     * 更新员工信息
     * @param employeeDTO 员工数据传输对象，包含要更新的员工信息
     */
    @Override
    public void updateEmp(EmployeeDTO employeeDTO) {
        // 将DTO对象转换为实体对象
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        // 设置更新时间和更新人信息
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 执行数据库更新操作
        employeeMapper.update(employee);
    }


}
