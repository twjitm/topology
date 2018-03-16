package prefuse.safein.demo;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

/**
 * Created by 文江 on 2018/3/16.
 */
public class ReadExcelUtils {
    public static void main(String[] args) {
        List<BlogCommentEntity> list = read();
    }

    public static List<BlogCommentEntity> read() {
        String eccelUtilsFilePath = "e:\\blog_comment_time_temp.xls";
        File file = new File(eccelUtilsFilePath);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Workbook workbook = Workbook.getWorkbook(fileInputStream);
            // Excel的页签数量
            int sheet_size = workbook.getNumberOfSheets();
            Sheet blog_commentSheet = workbook.getSheet(1);

            List<BlogCommentEntity> baseList = new ArrayList<>();
            long baseTop = 0;
            long tempTop = 0;
            for (int i = 3; i < blog_commentSheet.getRows() - 1; i++) {
                BlogCommentEntity baseEntity = new BlogCommentEntity();
                for (int j = 0; j < blog_commentSheet.getColumns(); j++) {
                    String cellinfo = blog_commentSheet.getCell(j, i).getContents();
                    if (j == 0) {
                        baseEntity.setRowLable(cellinfo);
                    } else {
                        baseEntity.setNumber(Long.parseLong(cellinfo));
                        tempTop += Long.parseLong(cellinfo);
                    }
                }
                //初始化数据
                if (i == 3) {
                    baseTop = baseEntity.getNumber();
                    List<BlogCommentEntity> sub = new ArrayList<>();
                    baseList.add(baseEntity);
                    baseEntity.setBlogCommentEntity(sub);
                    tempTop = 0;
                }
                if (tempTop <= baseTop && i != 3) {
                    baseList.get(baseList.size() - 1).getBlogCommentEntity().add(baseEntity);
                }
                if (tempTop > baseTop && i != 3) {
                    List<BlogCommentEntity> sub = new ArrayList<>();
                    baseTop = baseEntity.getNumber();
                    baseEntity.setBlogCommentEntity(sub);
                    baseList.add(baseEntity);
                    tempTop = 0;
                }
            }
            return baseList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static class BlogCommentEntity {
        private String rowLable;
        private long number;
        private List<BlogCommentEntity> BlogCommentEntity;

        public String getRowLable() {
            return rowLable;
        }

        public void setRowLable(String rowLable) {
            this.rowLable = rowLable;
        }

        public long getNumber() {
            return number;
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public List<ReadExcelUtils.BlogCommentEntity> getBlogCommentEntity() {
            return BlogCommentEntity;
        }

        public void setBlogCommentEntity(List<ReadExcelUtils.BlogCommentEntity> blogCommentEntity) {
            BlogCommentEntity = blogCommentEntity;
        }
    }

}
