package com.agileboot.admin.testsupport;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

public class ExternalControllerStub {

    @PreAuthorize("@permission.has('external:test:list')")
    @GetMapping("/external/test")
    public void test() {
    }

}
