package com.waylau.spring.boot.fileserver.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.waylau.spring.boot.fileserver.domain.File;
import com.waylau.spring.boot.fileserver.service.FileService;
import com.waylau.spring.boot.fileserver.util.MD5Util;

@CrossOrigin(origins = "*", maxAge = 3600)  // 允许所有域名访问
@Controller
public class FileController {

    @Autowired
    private FileService fileService;
    
    @Value("${server.address}")
    private String serverAddress;
    
    @Value("${server.port}")
    private String serverPort;
    
    @RequestMapping(value = "/")
    public String index(Model model,
    		@RequestParam(value="pageNo", required=false, defaultValue="1") String pageNoStr,
    		@RequestParam(value="pageSize", required=false, defaultValue="3") String pageSizeStr) {
    	
    	Integer pageNo = 1;
    	Integer pageSize = 3;
    	
        //校验pageNo
        pageNo = Integer.parseInt(pageNoStr);
        if(pageNo < 1){
            pageNo = 1;
        }
        
        //校验pageNo
        pageSize = Integer.parseInt(pageSizeStr);
        if(pageSize < 1){
        	pageSize = 3;
        }
        
        Page<File> pageList = fileService.pageInfo(pageNo, pageSize) ;
    	
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<File> fileList = pageList.getContent();
        for(File file:fileList){
        	file.setUploadDateStr(sf.format(file.getUploadDate()));
        }
        // 展示分页数据
        model.addAttribute("files", fileList);
        
        // 展示分页信息
        model.addAttribute("pages", pageList);
        
        // 展示每页条数
        model.addAttribute("pageSize", pageSize);
        
        System.out.println(fileList);
        
        return "index";
    }

    /**
     * 分页查询文件
     * @param pageIndex
     * @param pageSize
     * @return
     */
	@GetMapping("files/{pageIndex}/{pageSize}")
    @ResponseBody
	public List<File> listFilesByPage(@PathVariable int pageIndex, @PathVariable int pageSize){
		return fileService.listFilesByPage(pageIndex, pageSize);
	}
			
    /**
     * 获取文件片信息
     * @param id
     * @return
     */
    @GetMapping("files/{id}")
    @ResponseBody
    public ResponseEntity<Object> serveFile(@PathVariable String id) {

        File file = fileService.getFileById(id);

        if (file != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + file.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream" )
                    .header(HttpHeaders.CONTENT_LENGTH, file.getSize()+"")
                    .header("Connection",  "close") 
                    .body( file.getContent());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File was not fount");
        }

    }
    
    /**
     * 在线显示文件
     * @param id
     * @return
     */
    @GetMapping("/view/{id}")
    @ResponseBody
    public ResponseEntity<Object> serveFileOnline(@PathVariable String id) {

        File file = fileService.getFileById(id);
        if (file != null) {
        	
        	try {  
	            return ResponseEntity
	                    .ok()
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + file.getName() + "\"")
	                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType() )
	                    .header(HttpHeaders.CONTENT_LENGTH, file.getSize()+"")
	                    .header("Connection",  "close") 
	                    .body( file.getContent());
        	} catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } 
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File was not fount");


    }
    
    /**
     * 上传
     * @param file
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/create")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        try {
        	if(!file.isEmpty()){
        		File f = new File(file.getOriginalFilename(),  file.getContentType(), file.getSize(), file.getBytes());
            	f.setMd5( MD5Util.getMD5(file.getInputStream()) );
            	fileService.saveFile(f);
            	redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded " + file.getOriginalFilename() + "!");
        	}    	
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("message",
                    "Your " + file.getOriginalFilename() + " is wrong!");
            return "redirect:/";
        }

        return "redirect:/";
    }
 
    /**
     * 上传接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
    	File returnFile = null;
        try {
        	File f = new File(file.getOriginalFilename(),  file.getContentType(), file.getSize(),file.getBytes());
        	f.setMd5( MD5Util.getMD5(file.getInputStream()) );
        	returnFile = fileService.saveFile(f);
        	String path = "//"+ serverAddress + ":" + serverPort + "/view/"+returnFile.getId();
        	return ResponseEntity.status(HttpStatus.OK).body(path);
 
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
 
    }
    
    /**
     * 删除文件
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteFile(@PathVariable String id) {
 
    	try {
			fileService.removeFile(id);
			return ResponseEntity.status(HttpStatus.OK).body("DELETE Success!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
    }
    
    /**
     * 本工程页面删除文件
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable String id) {

			fileService.removeFile(id);
			return "redirect:/";

    }
}
