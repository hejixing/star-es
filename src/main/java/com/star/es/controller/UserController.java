package com.star.es.controller;

import com.star.es.domain.dto.UserSearchDTO;
import com.star.es.domain.entity.User;
import com.star.es.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 新增/修改 ES 数据
    @PostMapping("/save")
    public User save(@RequestBody User user) {
        return userService.save(user);
    }

    // 根据 ID 查询
    @GetMapping("/query/{id}")
    public User findById(@PathVariable("id") Long id) {
        return userService.findById(id).orElse(null);
    }

    // 分页查询
    @GetMapping("/list")
    public Page<User> list(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(page, size);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return "删除成功";
    }

    @PostMapping("/search")
    public Page<User> search(@RequestBody UserSearchDTO searchDTO) {
        return userService.searchByConditions(searchDTO);
    }
}
