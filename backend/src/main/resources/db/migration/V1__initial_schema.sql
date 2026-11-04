CREATE TABLE companies (
  id UUID PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  preferred_language VARCHAR(20) NOT NULL DEFAULT 'en',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_users (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies(id),
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','MANAGER','WORKER')),
  preferred_language VARCHAR(20) NOT NULL DEFAULT 'en',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies(id),
  name VARCHAR(180) NOT NULL,
  address TEXT,
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_assignments (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES projects(id),
  user_id UUID NOT NULL REFERENCES app_users(id),
  role_on_project VARCHAR(40) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(project_id, user_id)
);

CREATE TABLE uploads (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES projects(id),
  uploaded_by UUID NOT NULL REFERENCES app_users(id),
  upload_type VARCHAR(20) NOT NULL CHECK (upload_type IN ('AUDIO','PHOTO')),
  file_name VARCHAR(240) NOT NULL,
  file_path TEXT NOT NULL,
  content_type VARCHAR(120),
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  accuracy_meters DECIMAL(10,2),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reports (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES projects(id),
  created_by UUID NOT NULL REFERENCES app_users(id),
  approved_by UUID REFERENCES app_users(id),
  source_upload_id UUID REFERENCES uploads(id),
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  raw_transcript TEXT,
  structured_json TEXT,
  final_report TEXT,
  input_language VARCHAR(20),
  output_language VARCHAR(20) NOT NULL DEFAULT 'en',
  low_confidence BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approved_at TIMESTAMP
);

CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies(id),
  user_id UUID REFERENCES app_users(id),
  action VARCHAR(120) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id UUID,
  details TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_company ON projects(company_id);
CREATE INDEX idx_assignments_user ON project_assignments(user_id);
CREATE INDEX idx_reports_project ON reports(project_id);
CREATE INDEX idx_uploads_project ON uploads(project_id);
