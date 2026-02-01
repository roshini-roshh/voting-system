package dao;

import models.Voter;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoterDAO {

    // ==========================================================
    //                AUTHENTICATION & VOTING
    // ==========================================================

    /** Authenticate voter login */
    public Voter authenticateVoter(String voterId, String password) {

        String sql = "SELECT * FROM voters WHERE voter_id = ? AND password = ? AND is_approved = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractVoter(rs);

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    /** Get voter details */
    public Voter getVoterById(String voterId) {

        String sql = "SELECT * FROM voters WHERE voter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractVoter(rs);

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    /** Check if election is active (delegated to elections table) */
    public boolean isElectionActive() {

        String sql = "SELECT is_active FROM elections WHERE is_active = 1 LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next();

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** Check if voter already voted (boolean column has_voted) */
    public boolean hasVoted(String voterId) {

        String sql = "SELECT has_voted FROM voters WHERE voter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("has_voted");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /** Update voting status (set has_voted true/false) */
    public boolean updateVotingStatus(String voterId, boolean hasVoted) {

        String sql = "UPDATE voters SET has_voted = ? WHERE voter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, hasVoted);
            ps.setString(2, voterId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==========================================================
    //                     ADMIN – VOTER MANAGEMENT
    // ==========================================================

    /** Get *all* voters (approved + pending) */
    public List<Voter> getAllVoters() {

        String sql = "SELECT * FROM voters";
        List<Voter> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(extractVoter(rs));

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    /** Get approved voters */
    public List<Voter> getAllApprovedVoters() {

        String sql = "SELECT * FROM voters WHERE is_approved = 1";
        List<Voter> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(extractVoter(rs));

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    /** Get pending voters */
    public List<Voter> getPendingVoters() {

        String sql = "SELECT * FROM voters WHERE is_approved = 0";
        List<Voter> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(extractVoter(rs));

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // ==========================================================
    //                        CRUD OPERATIONS
    // ==========================================================

    /** Check roll number duplication */
    public boolean voterExists(String rollNumber) {

        String sql = "SELECT roll_number FROM voters WHERE roll_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rollNumber);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** Register voter */
    public boolean registerVoter(Voter v) {

        String sql = "INSERT INTO voters (voter_id, roll_number, full_name, department, year_of_study, email, password, is_approved, has_voted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String voterId = "v" + v.getRollNumber();

            ps.setString(1, voterId);
            ps.setString(2, v.getRollNumber());
            ps.setString(3, v.getFullName());
            ps.setString(4, v.getDepartment());
            ps.setString(5, v.getYearOfStudy());
            ps.setString(6, v.getEmail());
            ps.setString(7, v.getPassword());

            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** Update voter */
    public boolean updateVoter(Voter v) {

        String sql = "UPDATE voters SET full_name=?, department=?, year_of_study=?, email=?, password=?, is_approved=?, has_voted=? "
                + "WHERE voter_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getFullName());
            ps.setString(2, v.getDepartment());
            ps.setString(3, v.getYearOfStudy());
            ps.setString(4, v.getEmail());
            ps.setString(5, v.getPassword());
            ps.setBoolean(6, v.isApproved());
            ps.setBoolean(7, v.hasVoted());
            ps.setString(8, v.getVoterId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    // ==========================================================
    //                 IMPORTANT MISSING METHODS (ADDED)
    // ==========================================================

    /** Approve a voter */
    public boolean approveVoter(String voterId) {

        String sql = "UPDATE voters SET is_approved = 1 WHERE voter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** Delete / Reject a voter */
    public boolean deleteVoter(String voterId) {

        String sql = "DELETE FROM voters WHERE voter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    // ==========================================================
    //                      HELPER MAPPER
    // ==========================================================

    /** Map DB row → Voter model */
    private Voter extractVoter(ResultSet rs) throws Exception {

        Voter v = new Voter();
        v.setVoterId(rs.getString("voter_id"));
        v.setRollNumber(rs.getString("roll_number"));
        v.setFullName(rs.getString("full_name"));
        v.setDepartment(rs.getString("department"));
        v.setYearOfStudy(rs.getString("year_of_study"));
        v.setEmail(rs.getString("email"));
        v.setPassword(rs.getString("password"));
        v.setApproved(rs.getInt("is_approved") == 1);
        v.setHasVoted(rs.getBoolean("has_voted"));

        return v;
    }
}
