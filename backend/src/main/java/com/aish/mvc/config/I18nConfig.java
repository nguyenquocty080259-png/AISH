package com.aish.mvc.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Hạ tầng i18n cho THÔNG BÁO LỖI ngoài vùng auth. Ngôn ngữ lấy từ header Accept-Language do
 * frontend gắn theo lựa chọn i18n (mặc định 'vi', hỗ trợ 'en'); message tra theo locale trong
 * bộ messages*.properties. Chuỗi nào chưa rút thành key vẫn hiển thị nguyên văn (fallback ở
 * GlobalExceptionHandler), nên các lỗi auth chưa i18n không bị ảnh hưởng.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi");

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        // Không rơi về locale hệ điều hành: thiếu/khác locale -> dùng messages.properties (tiếng Việt).
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(VIETNAMESE);
        resolver.setSupportedLocales(List.of(VIETNAMESE, Locale.ENGLISH));
        return resolver;
    }

    // Cho phép message của bean-validation dạng {key} tra theo locale trong messages*.properties.
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        validator.afterPropertiesSet();
        return validator;
    }
}
