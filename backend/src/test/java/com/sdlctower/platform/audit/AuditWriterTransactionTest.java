package com.sdlctower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.sdlctower.platform.access.AssignRoleRequest;
import com.sdlctower.platform.access.PlatformAccessService;
import com.sdlctower.platform.access.RoleAssignmentRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;

@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditWriterTransactionTest {

    @Autowired
    private AuditWriter auditWriter;

    @Autowired
    private PlatformAccessService accessService;

    @Autowired
    private RoleAssignmentRepository roleAssignments;

    @SpyBean
    private AuditRepository auditRepository;

    @Test
    void auditWriterRequiresExistingTransaction() {
        assertThatThrownBy(() -> auditWriter.write(new AuditEvent(
                "system",
                "system",
                "permission_change",
                "test",
                "test",
                "test",
                "platform",
                "*",
                "success",
                null,
                Map.of("test", true)
        ))).isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void accessMutationRollsBackWhenAuditInsertFails() {
        doThrow(new RuntimeException("audit unavailable")).when(auditRepository).save(any(AuditRecordEntity.class));

        assertThatThrownBy(() -> accessService.assignRole(new AssignRoleRequest(
                "43910000",
                "WORKSPACE_ADMIN",
                "workspace",
                "ws-default-001"
        ), "43910516")).hasMessageContaining("audit unavailable");

        assertThat(roleAssignments.findByStaffIdAndRoleAndScopeTypeAndScopeId(
                "43910000",
                "WORKSPACE_ADMIN",
                "workspace",
                "ws-default-001"
        )).isEmpty();
    }
}
