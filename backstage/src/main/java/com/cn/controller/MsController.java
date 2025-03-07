package com.cn.controller;

import com.cn.*;
import com.cn.entity.*;
import com.cn.util.RestUtil;
import com.cn.util.UploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 音书管理 控制类
 *
 * @author ngcly
 */
@Controller
@RequestMapping("/ms")
@AllArgsConstructor
public class MsController {
    private final UserService userService;
    private final RoleService roleService;
    private final ClassifyService classifyService;
    private final NoticeService noticeService;
    private final CarouselService carouselService;
    private final EssayService essayService;

    /**
     * 用户列表页
     */
    @PreAuthorize("hasAuthority('user:view')")
    @RequestMapping("/user")
    public String userList() {
        return "user/userList";
    }

    /**
     * 获取用户列表信息
     */
    @ResponseBody
    @RequestMapping("/userList")
    public ModelMap getUserList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                @RequestParam(value = "size", defaultValue = "10") Integer size, User user) {
        Page<User> userList = userService.getUserList(PageRequest.of(page - 1, size), user);
        return RestUtil.success(userList.getTotalElements(), userList.getContent());
    }

    /**
     * 用户编辑页
     */
    @RequestMapping("/userEdit")
    public String userEdit(@RequestParam Long userId, Model model) {
        User user = userService.getUserDetail(userId);
        Set<Role> roles = roleService.getAvailableRoles(Role.ROLE_TYPE_CUSTOMER);
        roles.addAll(user.getRoleList());
        String checkRoleIds = user.getRoleList().stream().map(role ->
                role.getId().toString()).collect(Collectors.joining(","));
        model.addAttribute(user);
        //待选角色列表
        model.addAttribute("roles", roles);
        //已勾选角色ID
        model.addAttribute("checkRoleId", checkRoleIds);
        return "user/userEdit";
    }

    /**
     * 用户详情页
     */
    @RequestMapping("/userDetail")
    public String userDetail(@RequestParam Long userId, Model model) {
        User user = userService.getUserDetail(userId);
        model.addAttribute(user);
        return "user/userDetail";
    }

    /**
     * 修改用户
     */
    @PreAuthorize("hasAuthority('user:alt')")
    @ResponseBody
    @RequestMapping("/userSave")
    public ModelMap saveUser(@Valid User user) {
        userService.altUser(user);
        return RestUtil.success();

    }

    /**
     * 删除用户
     */
    @PreAuthorize("hasAuthority('user:del')")
    @ResponseBody
    @RequestMapping("/userDel")
    public ModelMap delUser(@RequestParam Long userId) {
        userService.delUser(userId);
        return RestUtil.success();

    }

    /**
     * 分类列表页
     */
    @PreAuthorize("hasAuthority('clazz:view')")
    @RequestMapping("/classify")
    public String classifyList() {
        return "classify/classifyList";
    }

    /**
     * 获取分类列表
     */
    @ResponseBody
    @RequestMapping("/classifyList")
    public ModelMap getClassifyList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size, Classify classify) {
        Page<Classify> classifyList = classifyService.getClassifyList(PageRequest.of(page - 1, size), classify);
        return RestUtil.success(classifyList.getTotalElements(), classifyList.getContent());
    }

    /**
     * 修改分类页
     */
    @RequestMapping("/classifyAlt")
    public String altClassify(@RequestParam(required = false) Long id, Model model) {
        Classify classify = new Classify();
        if (id != null) {
            classify = classifyService.getClassifyDetail(id);
        }
        model.addAttribute(classify);
        return "classify/classifyEdit";
    }

    /**
     * 保存分类
     */
    @PreAuthorize("hasAnyAuthority('clazz:add','clazz:alt')")
    @ResponseBody
    @PostMapping("/saveClassify")
    public ModelMap saveClassify(@Valid Classify classify) {
        classifyService.saveClassify(classify);
        return RestUtil.success();

    }

    /**
     * 删除分类
     */
    @PreAuthorize("hasAuthority('clazz:del')")
    @ResponseBody
    @GetMapping("/classifyDel/{id}")
    public ModelMap delClassify(@PathVariable("id") Long id) {
        classifyService.deleteClassify(id);
        return RestUtil.success();
    }

    /**
     * 文章列表页
     */
    @PreAuthorize("hasAuthority('story:view')")
    @RequestMapping("/essay")
    public String essayList(Model model) {
        model.addAttribute("classifyList", classifyService.getClassifyList());
        return "essay/essayList";
    }

    /**
     * 获取文章列表
     */
    @ResponseBody
    @RequestMapping("/essayList")
    public ModelMap getEssayList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size, Essay essay) {
        Page<Essay> essayList = essayService.getEssayList(PageRequest.of(page - 1, size, Sort.by("createdAt").descending()), essay);
        return RestUtil.success(essayList.getTotalElements(), essayList.getContent());
    }

    /**
     * 文章审查页
     */
    @RequestMapping("/essayAlt")
    public String essayAlt(@RequestParam Long id, Model model) {
        Essay essay = essayService.getEssayDetail(id);
        model.addAttribute(essay);
        return "essay/essayEdit";
    }

    /**
     * 文章审查
     */
    @PreAuthorize("hasAuthority('story:alt')")
    @ResponseBody
    @PostMapping("/essaySave")
    public ModelMap essaySave(@Valid Essay essay) {
        essayService.altEssayState(essay);
        return RestUtil.success();
    }

    /**
     * 公告列表页
     */
    @PreAuthorize("hasAuthority('not:view')")
    @RequestMapping("/notice")
    public String noticeList() {
        return "notice/noticeList";
    }

    /**
     * 获取公告列表
     */
    @ResponseBody
    @GetMapping("/noticeList")
    public ModelMap getNoticeList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "size", defaultValue = "10") Integer size, Notice notice) {
        Page<Notice> notices = noticeService.getNoticeList(PageRequest.of(page - 1, size), notice);
        return RestUtil.success(notices.getTotalElements(), notices.getContent());
    }

    /**
     * 修改公告页
     */
    @RequestMapping("/noticeAlt")
    public String altNotice(@RequestParam(required = false) Long id, Model model) {
        Notice notice = new Notice();
        if (id != null) {
            notice = noticeService.getNoticeDetail(id);
        }
        model.addAttribute(notice);
        return "notice/noticeEdit";
    }

    /**
     * 保存公告
     */
    @PreAuthorize("hasAnyAuthority('not:add','not:alt')")
    @ResponseBody
    @PostMapping("/saveNotice")
    public ModelMap saveNotice(@Valid Notice notice) {
        noticeService.addOrUpdateNotice(notice);
        return RestUtil.success();
    }

    /**
     * 删除公告
     */
    @PreAuthorize("hasAuthority('not:del')")
    @ResponseBody
    @GetMapping("/noticeDel/{id}")
    public ModelMap delNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return RestUtil.success();
    }

    /**
     * 轮播图页
     */
    @PreAuthorize("hasAuthority('sel:view')")
    @RequestMapping("/carousel")
    public String carouselList() {
        return "carousel/carouselList";
    }

    /**
     * 获取轮播图列表
     */
    @ResponseBody
    @GetMapping("/carouselList")
    public ModelMap carouselList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size, String name) {
        Page<CarouselCategory> carouselCategory = carouselService.getCarouselList(name, PageRequest.of(page - 1, size));
        return RestUtil.success(carouselCategory.getTotalElements(), carouselCategory.getContent());
    }

    /**
     * 修改轮播分类页
     */
    @RequestMapping("/carouselCategoryAlt")
    public String altCarouselCategory(@RequestParam(required = false) Long id, Model model) {
        CarouselCategory carouselCategory = new CarouselCategory();
        if (id != null) {
            carouselCategory = carouselService.getCarouselDetail(id);
        }
        model.addAttribute(carouselCategory);
        return "carousel/carouselCategoryEdit";
    }

    /**
     * 修改轮播图页
     */
    @RequestMapping("/carouselAlt")
    public String altCarousel(@RequestParam Long id, Model model) {
        CarouselCategory carouselCategory = carouselService.getCarouselDetail(id);
        model.addAttribute(carouselCategory);
        return "carousel/carouselEdit";
    }

    /**
     * 保存轮播图
     */
    @PreAuthorize("hasAnyAuthority('sel:add','sel:alt')")
    @ResponseBody
    @PostMapping("/saveCarousel")
    public ModelMap saveCarousel(@Valid CarouselCategory carouselCategory) {
        carouselService.addOrUpdateCarousel(carouselCategory);
        return RestUtil.success();
    }

    /**
     * 添加轮播图
     */
    @PreAuthorize("hasAnyAuthority('sel:add','sel:alt')")
    @ResponseBody
    @RequestMapping("/addCarousel")
    public ModelMap addCarousel(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        if (file.isEmpty()) {
            return RestUtil.failure(222, "文件为空");
        }
        String path = UploadUtil.uploadFileByAli(file, "img");
        carouselService.addCarousel(id, path);
        return RestUtil.success();
    }

    /**
     * 删除轮播分类
     */
    @PreAuthorize("hasAuthority('sel:del')")
    @ResponseBody
    @GetMapping("/carouselDel/{id}")
    public ModelMap delCarouselCategory(@PathVariable("id") Long id) {
        carouselService.deleteCarouselCategory(id);
        return RestUtil.success();
    }

    /**
     * 删除轮播图
     */
    @PreAuthorize("hasAuthority('sel:del')")
    @ResponseBody
    @GetMapping("/carouselDel")
    public ModelMap delCarousel(@RequestParam("id") Long id, @RequestParam("url") String url) {
        carouselService.deleteCarousel(id, url);
        return RestUtil.success();
    }

}
