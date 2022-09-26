package com.shiiiiiit.demo;

import com.microsoft.playwright.Playwright;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class VkAuthThread extends Thread {
//    private final User user;
//    private final VKAuthRequest request;
    private final Logger logger = LoggerFactory.getLogger(VkAuthThread.class);

    public VkAuthThread(
            String name
//            User user,
//            VKAuthRequest request
    ) {
        super(name);
//        this.user = user;
//        this.request = request;
    }

    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public void run() {
        try (
                Playwright playwright = Playwright.create();
                var browser = playwright.chromium().launch();
        ) {
            var vkService = ApplicationContextHolder.getApplicationContext().getBean(VkService.class);

            var emailOrPhone = "email";
            var password = "password";

            var page = browser.newPage();
            var authUrl = "https://oauth.vk.com/authorize?client_id=8219803&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=140487903&response_type=token&v=5.131&state=null";
            page.navigate(authUrl);
            System.out.println(page.url());
            // авторизация
            var nameInput = page.locator("input.oauth_form_input:nth-child(7)");
            nameInput.type(emailOrPhone);

            var passwordInput = page.locator("input.oauth_form_input:nth-child(9)");
            passwordInput.type(password);

            var submit1 = page.locator("button.flat_button");
            submit1.click();

            System.out.println(page.url());
            // подтверждение привязки приложения

            try {
                var submit2 = page.locator("button.button_indent");
                submit2.click();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            System.out.println(page.url());

            // либо финал с токеном либо подтверждение кода
            var finalUrl = page.url().substring(0, 26);
            if (finalUrl.equals("https://oauth.vk.com/login") || finalUrl.equals("https://oauth.vk.com/blank")) {
                if (finalUrl.equals("https://oauth.vk.com/login")) {
                    String code;
                    while (true) {
                        var enqueuedValue = vkService.getQueuedValue();

                        System.out.println(enqueuedValue.getFirst());

                        if (enqueuedValue.getFirst().equals("aboba")) {
                            code = enqueuedValue.getSecond();
                            System.out.println(code);
                            break;
                        } else {
                            vkService.enqueueValue(enqueuedValue);
                            // TODO: fix when thread too fast to read
                        }
                    }

                    System.out.println("TAPPPP BEFORE");
                    var codeInput = page.locator("input.TextInput__native");
//                    codeInput.type(code);
//                    codeInput.fill(code);
                    page.addInitScript(String.format("document.querySelector('input.TextInput__native').value = %s", code));
                    System.out.println(codeInput.allInnerTexts());
//                    codeInput.fill();

                    var permitButton = page.locator("input.button");
                    permitButton.click();
                    System.out.println(page.url());
                }

                var preparedUrl = StringUtils.replaceOnce(page.url(), "#", "?");
                logger.debug(String.format("Parsed url: %s", preparedUrl));

                var authorizedPageUrl = getQueryMap(new URI(preparedUrl).getRawQuery());
                logger.debug(String.format("Access token: %s", authorizedPageUrl.get("access_token")));

                System.out.println("access_token: " + authorizedPageUrl.get("access_token"));
                System.out.println("user_id: " + authorizedPageUrl.get("user_id"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

