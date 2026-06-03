package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.CheckerType;
import com.example.linuxlearning.domain.Course;
import com.example.linuxlearning.domain.CourseModule;
import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.LabTask;
import com.example.linuxlearning.domain.LearningPath;
import com.example.linuxlearning.domain.Lesson;
import com.example.linuxlearning.domain.PublishStatus;
import com.example.linuxlearning.domain.Role;
import com.example.linuxlearning.domain.SandboxTier;
import com.example.linuxlearning.domain.UserAccount;
import com.example.linuxlearning.repository.CourseRepository;
import com.example.linuxlearning.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final UserAccountRepository userAccountRepository;

    public DemoDataInitializer(CourseRepository courseRepository, UserAccountRepository userAccountRepository) {
        this.courseRepository = courseRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            return;
        }
        userAccountRepository.save(new UserAccount("student", "默认学员", Role.STUDENT, LearningPath.BEGINNER));
        userAccountRepository.save(new UserAccount("ops", "运维提升学员", Role.STUDENT, LearningPath.OPS));
        userAccountRepository.save(new UserAccount("admin", "内容管理员", Role.ADMIN, LearningPath.OPS));
        courseRepository.save(buildCourse());
    }

    private Course buildCourse() {
        Course course = new Course(
                "Linux 手敲实战路线",
                "从零基础命令行到服务部署、排障、Docker 与 K8s 入门的真实 VM 实验路径。",
                PublishStatus.PUBLISHED,
                1
        );
        course.addModule(basicModule());
        course.addModule(serviceModule());
        course.addModule(containerModule());
        return course;
    }

    private CourseModule basicModule() {
        CourseModule module = new CourseModule("阶段 1：Linux 基础", "能独立使用终端完成目录、用户、文件和权限操作。", 1);
        Lesson lesson = new Lesson("进入终端与目录定位", "理解当前目录、用户身份和基本文件操作。", 25, 1);
        Lab lab = new Lab(
                "基础命令热身",
                "在真实 VM 里完成 pwd、whoami、touch 等基础命令，并由系统检查输出和文件痕迹。",
                "ubuntu-22.04-foundation",
                1,
                1024,
                30,
                "mirrors.aliyun.com,repo.huaweicloud.com,platform.internal",
                SandboxTier.VM,
                PublishStatus.PUBLISHED
        );
        lab.addTask(new LabTask(
                "确认当前目录",
                "执行 `pwd`，确认你位于学员默认工作目录。",
                CheckerType.OUTPUT_CONTAINS,
                "/home/student",
                "直接在终端输入 pwd 并回车。",
                10,
                1
        ));
        lab.addTask(new LabTask(
                "创建权限练习文件",
                "执行 `touch /tmp/linux-foundation/permission-ok`，为后续权限实验准备文件。",
                CheckerType.FILE_EXISTS,
                "/tmp/linux-foundation/permission-ok",
                "如果目录不存在，先 mkdir -p /tmp/linux-foundation。",
                15,
                2
        ));
        lesson.assignLab(lab);
        module.addLesson(lesson);
        return module;
    }

    private CourseModule serviceModule() {
        CourseModule module = new CourseModule("阶段 2：服务与排障", "能查看服务状态、读取日志，并定位常见部署问题。", 2);
        Lesson lesson = new Lesson("检查 Web 服务状态", "使用 systemctl 和日志命令确认服务是否正常运行。", 35, 1);
        Lab lab = new Lab(
                "Nginx 服务状态检查",
                "在 VM 中检查 Nginx 服务运行状态，理解 active、failed 和日志定位的区别。",
                "ubuntu-22.04-nginx",
                2,
                2048,
                45,
                "mirrors.aliyun.com,nginx.org,platform.internal",
                SandboxTier.VM,
                PublishStatus.PUBLISHED
        );
        lab.addTask(new LabTask(
                "确认服务 active",
                "执行 `systemctl status nginx`，确认服务处于 active running 状态。",
                CheckerType.SERVICE_ACTIVE,
                "active (running)",
                "关注输出里的 Active 行。",
                20,
                1
        ));
        lesson.assignLab(lab);
        module.addLesson(lesson);
        return module;
    }

    private CourseModule containerModule() {
        CourseModule module = new CourseModule("阶段 3：Docker 与 K8s", "理解容器运行、镜像拉取、Pod 状态和基础部署检查。", 3);
        Lesson lesson = new Lesson("查看 K8s 工作负载", "通过 kubectl 检查 Pod 是否处于 Running 状态。", 40, 1);
        Lab lab = new Lab(
                "K8s 入门状态检查",
                "在预置集群节点中执行 kubectl 命令，识别 Pod READY、STATUS 和重启次数。",
                "kubevirt-k8s-baseline",
                2,
                4096,
                60,
                "registry.aliyuncs.com,kubernetes.io,platform.internal",
                SandboxTier.KUBEVIRT,
                PublishStatus.PUBLISHED
        );
        lab.addTask(new LabTask(
                "确认 Pod Running",
                "执行 `kubectl get pods`，确认演示工作负载处于 Running 状态。",
                CheckerType.OUTPUT_CONTAINS,
                "running",
                "先观察 STATUS 列，Running 表示容器已成功启动。",
                25,
                1
        ));
        lesson.assignLab(lab);
        module.addLesson(lesson);
        return module;
    }
}
