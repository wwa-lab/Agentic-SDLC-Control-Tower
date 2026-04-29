UPDATE requirement_source_reference
SET url = 'jira://AUTH-123'
WHERE id = 'SRC-REQ-0001-JIRA'
  AND url = 'https://jira.example.com/browse/AUTH-123';

UPDATE requirement_source_reference
SET url = 'confluence://AUTH-SSO'
WHERE id = 'SRC-REQ-0001-CONF'
  AND url = 'https://confluence.example.com/display/AUTH/SSO';
