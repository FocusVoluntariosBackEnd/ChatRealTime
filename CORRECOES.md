DTOS de user e auth, deixar somente um, sem necessidade de dto por camada


usecase e servive , padrao de nome S SOLID, de user e auth


Em UserService.java 
(src/main/java/com/focustecnologia/chat_real_time/user/service/UserService.java:31), 
Math.min(0, page - 1) faz todas as páginas a partir da 1 consultarem a página 0. 
Assim, a paginação anunciada por GET /api/users não funciona corretamente


GET /api/users/{id} lança RuntimeException quando o usuário não existe, 
embora o controller documente resposta 404; 
isso precisa ser alinhado. 



