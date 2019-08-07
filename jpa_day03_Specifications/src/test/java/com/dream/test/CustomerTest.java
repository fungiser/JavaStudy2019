package com.dream.test;


import com.dream.dao.CustomerDao;
import com.dream.pojo.Customer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.criteria.*;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class) //声明spring提供的单元测试环境
@ContextConfiguration(locations = "classpath:applicationContext.xml")//指定spring容器的配置信息
public class CustomerTest {

    @Autowired
    private CustomerDao customerDao;

    @Test
    public void testSpecifications1() {
        //匿名内部类
        /**
         * 自定义查询条件
         *      1.实现Specification接口（提供泛型：查询的对象类型）
         *      2.实现toPredicate方法（构造查询条件）
         *      3.需要借助方法参数中的两个参数（
         *          root：获取需要查询的对象属性
         *          CriteriaBuilder：构造查询条件的，内部封装了很多的查询条件（模糊匹配，精准匹配）
         *       ）
         *  案例：根据客户名称查询，模糊查询客户名为"黑马"的客户
         *          查询条件
         *              1.查询方式
         *                  cb对象
         *              2.比较的属性名称
         *                  root对象
         *
         */
        Specification<Customer> specification = new Specification<Customer>() {
            public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path<Object> custName = root.get("custName");
                Predicate predicate = criteriaBuilder.equal(custName, "黑马程序员2");
                return predicate;
            }
        };

        Customer customer = customerDao.findOne(specification).get();
        System.out.println(customer);
    }

    /**
     * 多条件查询
     * 案例：根据客户名（传智播客）和客户所属行业查询（it教育）
     */
    @Test
    public void testSpec1() {
        Specification<Customer> specification = new Specification<Customer>() {
            @Override
            public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path<Object> custName = root.get("custName");
                Path<Object> custIndustry = root.get("custIndustry");
                Predicate predicate1 = criteriaBuilder.equal(custName, "190802_jpa学习");
                Predicate predicate2 = criteriaBuilder.equal(custIndustry, "学习");

                Predicate predicate = criteriaBuilder.and(predicate1, predicate2);
                return predicate;
            }
        };
        Customer customer = customerDao.findOne(specification).get();
        System.out.println(customer);
    }

    /**
     * 案例：完成根据客户名称的模糊匹配，返回客户列表
     * 客户名称以 ’传智播客‘ 开头
     * <p>
     * equal ：直接的到path对象（属性），然后进行比较即可
     * gt，lt,ge,le,like : 得到path对象，根据path指定比较的参数类型，再去进行比较
     * 指定参数类型：path.as(类型的字节码对象)
     */
    @Test
    public void testSpec3() {
        //条件查询
        Specification<Customer> specification = new Specification<Customer>() {
            @Override
            public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path<Object> custName = root.get("custName");
                Predicate predicate = criteriaBuilder.like(custName.as(String.class), "%19%");
                return predicate;
            }
        };
        //排序
        Sort sort = new Sort(Sort.Direction.DESC, "custId");
        List<Customer> list = customerDao.findAll(specification, sort);
        for (Customer customer : list) {
            System.out.println(customer);
        }

    }

    /**
     * 分页查询
     * Specification: 查询条件
     * Pageable：分页参数
     * 分页参数：查询的页码，每页查询的条数
     * findAll(Specification,Pageable)：带有条件的分页
     * findAll(Pageable)：没有条件的分页
     * 返回：Page（springDataJpa为我们封装好的pageBean对象，数据列表，共条数）
     */
    @Test
    public void testSpec4() {

        Specification spec = null;
        //PageRequest对象是Pageable接口的实现类
        /**
         * 创建PageRequest的过程中，需要调用他的构造方法传入两个参数
         *      第一个参数：当前查询的页数（从0开始）
         *      第二个参数：每页查询的数量
         */
        Sort sort = new Sort(Sort.Direction.DESC, "custId");
        Pageable pageable = PageRequest.of(0, 2, sort);
        //分页查询
        Page<Customer> page = customerDao.findAll(pageable);
        //Page<Customer> page = customerDao.findAll(pageable.next());//下一页
        System.out.println(page.getContent()); //得到数据集合列表
        System.out.println(page.getTotalElements());//得到总条数
        System.out.println(page.getTotalPages());//得到总页数

    }
}
