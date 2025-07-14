一个 **Minecraft Forge Mod**，用于禁止游戏内特定区域刷怪。

只在 **1.20.1 Forge** 上测试过可用。  
*なつき不太懂JAVA，本项目基本上为全AI生成的代码w*

---

## 使用方式

*   **创建无怪物区：** `/nomonster add <name> <center> <range>`
    *   `<name>`：自定义名称标记
    *   `<center>`：无怪物区中心点坐标
    *   `<range>`：无怪物区范围
    *   无怪物区的范围是水平以 `<center>` 为圆心，`<range>` 为半径的圆形范围，垂直无限。

*   **删除无怪物区：** `/nomonster delete <name>`

*   **列出所有无怪物区：** `/nomonster list`

---

> 无怪物区内包括刷怪蛋、指令等所有怪物生成都会被影响，但不会影响现有的怪物和从区域外进来的怪物。
