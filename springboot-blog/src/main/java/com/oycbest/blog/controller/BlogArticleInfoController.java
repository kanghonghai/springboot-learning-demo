package com.oycbest.blog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oycbest.blog.entity.BlogArticle;
import com.oycbest.blog.entity.BlogArticleInfo;
import com.oycbest.blog.service.BlogArticleInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 文章表(BlogArticleInfo)表控制层
 *
 * @author oyc
 * @since 2020-12-16 11:16:54
 */
@RestController
@RequestMapping("articleInfo")
public class BlogArticleInfoController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private BlogArticleInfoService blogArticleInfoService;

    /**
     * 分页查询所有数据
     *
     * @param page            分页对象
     * @param blogArticleInfo 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<BlogArticleInfo> page, BlogArticleInfo blogArticleInfo) {
        return success(this.blogArticleInfoService.page(page, new QueryWrapper<>(blogArticleInfo)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.blogArticleInfoService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param blogArticleInfo 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody BlogArticleInfo blogArticleInfo) {
        return success(this.blogArticleInfoService.save(blogArticleInfo));
    }

    /**
     * 修改数据
     *
     * @param blogArticleInfo 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody BlogArticleInfo blogArticleInfo) {
        return success(this.blogArticleInfoService.updateById(blogArticleInfo));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Integer> idList) {
        return success(this.blogArticleInfoService.removeByIds(idList));
    }

    /**
     * 根据用户id获取文章列表
     *
     * @param userId
     * @param page
     * @return
     */
    @GetMapping("userId/{userId}")
    public R getArticleByUserId(@PathVariable Integer userId, Page<BlogArticleInfo> page) {
        BlogArticleInfo blogArticleInfo = new BlogArticleInfo();
        blogArticleInfo.setUserId(userId);
        return success(this.blogArticleInfoService.page(page, new QueryWrapper<>(blogArticleInfo)));
    }

}
