package com.cn.pojo;

import com.cn.entity.Music;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ngcly
 */
@Data
@Schema(title="创建文章参数", description = "创建文章需要参数")
public class EssayDTO {

    @Schema(title="主键")
    private String id;

    @Schema(title="标题", required = true)
    @NotBlank(message = "标题不可为空")
    private String title;

    @Schema(title="分类", required = true)
    @NotNull(message = "分类不可为空")
    private Long classifyId;

    @Schema(title="简介", required = true)
    private String synopsis;

    @Schema(title="内容", required = true)
    @NotBlank(message = "内容不可为空")
    private String content;

    @Schema(title="音乐列表")
    private List<Music> musicList;

}
