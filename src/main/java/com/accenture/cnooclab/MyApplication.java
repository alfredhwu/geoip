package com.accenture.cnooclab;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebAppService;
import com.github.thorqin.toolkit.web.annotation.WebRouter;

@WebApp(name = "geoip",
        routers = {
                @WebRouter("*.do")
                // Uncomment following line to enable database router
                // , @WebRouter(value = "/db/*", type = MyDBRouter.class)
        },
        service = {
                @WebAppService(
                        name = "db",
                        type = DBService.class
                )
        }
)
public class MyApplication extends WebApplication {

}

/* Uncomment following lines to enable database router

@DBRouter
class MyDBRouter extends WebDBRouter {
    public MyDBRouter(WebApplication application) throws ValidateException {
        super(application);
    }
}

*/
