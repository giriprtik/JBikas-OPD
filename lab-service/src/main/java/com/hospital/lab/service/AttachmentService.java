package com.hospital.lab.service;

import com.hospital.lab.dto.AttachmentResponse;
import com.hospital.lab.exception.BusinessRuleException;
import com.hospital.lab.exception.NotFoundException;
import com.hospital.lab.model.LabAttachment;
import com.hospital.lab.model.LabOrder;
import com.hospital.lab.model.LabOrderStatus;
import com.hospital.lab.repository.LabAttachmentRepository;
import com.hospital.lab.security.CurrentUser;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

/**
 * Upload of scanned reports, analyser printouts and images. Only PDF, PNG and JPEG are accepted, and the
 * type is decided from the file's first bytes, not from the name or the browser's Content-Type.
 */
@Service
public class AttachmentService {

    public record StoredFile(Resource resource, String contentType, String filename) {
    }

    private final LabAttachmentRepository attachments;
    private final LabOrderService orderService;
    private final AttachmentStorage storage;
    private final Clock clock;

    public AttachmentService(LabAttachmentRepository attachments, LabOrderService orderService,
                             AttachmentStorage storage, Clock clock) {
        this.attachments = attachments;
        this.orderService = orderService;
        this.storage = storage;
        this.clock = clock;
    }

    @Transactional
    public AttachmentResponse upload(Long orderId, MultipartFile file, String description) {
        LabOrder order = orderService.find(orderId);
        if (order.getStatus() == LabOrderStatus.CANCELLED) {
            throw new BusinessRuleException("Files cannot be attached to a cancelled order.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The file is empty.");
        }

        String key;
        String contentType;
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            in.mark(8);
            byte[] head = in.readNBytes(8);
            in.reset();
            contentType = sniff(head);
            if (contentType == null) {
                throw new IllegalArgumentException("Only PDF, PNG and JPEG files can be attached.");
            }
            key = storage.save(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }

        try {
            LabAttachment a = new LabAttachment();
            a.setOrder(order);
            a.setOriginalFilename(safeName(file.getOriginalFilename(), contentType));
            a.setContentType(contentType);
            a.setSizeBytes(file.getSize());
            a.setStorageKey(key);
            a.setDescription(description == null || description.isBlank() ? null : description.trim());
            a.setUploadedBy(CurrentUser.username());
            a.setUploadedAt(Instant.now(clock));
            return LabMapper.toResponse(attachments.save(a));
        } catch (RuntimeException e) {
            storage.delete(key); // keep disk and database in step
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public StoredFile download(Long orderId, Long attachmentId) {
        LabAttachment a = find(orderId, attachmentId);
        return new StoredFile(storage.load(a.getStorageKey()), a.getContentType(), a.getOriginalFilename());
    }

    /** Allowed until the report is released; after that the file is part of the record. */
    @Transactional
    public void delete(Long orderId, Long attachmentId) {
        LabAttachment a = find(orderId, attachmentId);
        if (a.getOrder().getStatus() == LabOrderStatus.RELEASED) {
            throw new BusinessRuleException("Attachments of a released report cannot be removed.");
        }
        attachments.delete(a);
        storage.delete(a.getStorageKey());
    }

    private LabAttachment find(Long orderId, Long attachmentId) {
        return attachments.findByIdAndOrderId(attachmentId, orderId)
                .orElseThrow(() -> new NotFoundException("Attachment " + attachmentId + " not found on order " + orderId + "."));
    }

    static String sniff(byte[] head) {
        if (startsWith(head, new byte[]{'%', 'P', 'D', 'F'})) {
            return "application/pdf";
        }
        if (startsWith(head, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "image/png";
        }
        if (startsWith(head, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        return null;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        return data.length >= prefix.length && Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }

    /** File name without any path, limited to 200 characters, with an extension matching the real type. */
    private static String safeName(String original, String contentType) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(original == null ? "" : original));
        if (name == null || name.isBlank() || name.contains("..")) {
            name = "attachment";
        }
        name = name.replaceAll("[\\r\\n\"\\\\]", "_");
        if (name.length() > 200) {
            name = name.substring(name.length() - 200);
        }
        String ext = switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
        return name.toLowerCase().endsWith(ext) || (ext.equals(".jpg") && name.toLowerCase().endsWith(".jpeg")) ? name : name + ext;
    }
}
