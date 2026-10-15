package com.hospital.lab.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AttachmentSniffTest {

    @Test
    void recognisesAllowedTypesByContent() {
        assertEquals("application/pdf", AttachmentService.sniff("%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
        assertEquals("image/png", AttachmentService.sniff(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}));
        assertEquals("image/jpeg", AttachmentService.sniff(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}));
    }

    @Test
    void rejectsEverythingElseEvenWithAPdfName() {
        assertNull(AttachmentService.sniff("<html><script>".getBytes(StandardCharsets.US_ASCII)));
        assertNull(AttachmentService.sniff(new byte[]{'M', 'Z'}));
        assertNull(AttachmentService.sniff(new byte[0]));
    }
}
