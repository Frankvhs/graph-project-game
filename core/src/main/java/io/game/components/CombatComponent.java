package io.game.components;

public class CombatComponent {
    private int damage;
    
    private float attackCooldown;
    private float cooldownTimer = 0f;

    private float attackRange;

    private boolean attacking = false;

    public CombatComponent(int baseDamage, float attackCooldown, float attackRange) {
        this.damage = baseDamage;
        this.attackCooldown = attackCooldown;
        this.attackRange = attackRange;
    }

    public void update(float delta) {
        if (cooldownTimer > 0)
            cooldownTimer -= delta;
    }

    public boolean tryAttack() {
        if (cooldownTimer <= 0) {
            attacking = true;
            cooldownTimer = attackCooldown;
            return true;
        }
        return false;
    }

    public void finishAttack() {
        attacking = false;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public int getDamage() {
        return damage;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getCooldownPercent() {
        if (attackCooldown == 0) return 1;
        return 1f - (cooldownTimer / attackCooldown);
    }
}
