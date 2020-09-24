package com.leyou.client;

import com.leyou.item.api.CategroyApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface CategoryClient extends CategroyApi {
}
